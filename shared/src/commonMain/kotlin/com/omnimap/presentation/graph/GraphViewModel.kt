package com.omnimap.presentation.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.omnimap.core.connectivity.ConnectivityObserver
import com.omnimap.core.haptics.HapticEngine
import com.omnimap.data.remote.dto.AiMutationResponse
import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.Node
import com.omnimap.domain.model.NodeType
import com.omnimap.domain.model.QueuedAiRequest
import com.omnimap.domain.repository.AiInferenceRepository
import com.omnimap.domain.repository.OmniMapRepository
import com.omnimap.presentation.graph.chat.ChatMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GraphViewModel(
    private val repository: OmniMapRepository,
    private val hapticEngine: HapticEngine,
    private val aiRepository: AiInferenceRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _state = MutableStateFlow(GraphState(isLoading = true))
    val state: StateFlow<GraphState> = _state.asStateFlow()

    private val intentChannel = Channel<GraphIntent>(Channel.UNLIMITED)

    init {
        handleIntents()
        observeGraphData()
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                if (status == ConnectivityObserver.Status.Available) {
                    retryQueuedRequests()
                }
            }
        }
    }

    fun processIntent(intent: GraphIntent) {
        viewModelScope.launch {
            intentChannel.send(intent)
        }
    }

    private fun handleIntents() {
        viewModelScope.launch {
            intentChannel.consumeAsFlow().collect { intent ->
                when (intent) {
                    is GraphIntent.LoadGraph -> {
                        // Initially handled by observeGraphData
                    }
                    is GraphIntent.OnNodeDragged -> updateNodePosition(intent.id, intent.newX, intent.newY)
                    is GraphIntent.OnNodeCreated -> createNode(intent)
                    is GraphIntent.OnEdgeCreated -> createEdge(intent)
                    is GraphIntent.OnNodeDeleted -> deleteNode(intent.id)
                    is GraphIntent.OnEdgeDeleted -> deleteEdge(intent.id)
                    is GraphIntent.OnNodeSelected -> selectNode(intent.id)
                    is GraphIntent.OnSubmitPrompt -> submitPrompt(intent.prompt)
                    is GraphIntent.OnEditNodeRequest -> editNodeRequest(intent.id)
                    is GraphIntent.OnNodeUpdated -> updateNodeData(intent.id, intent.newTitle, intent.newDescription)
                    is GraphIntent.OnCreateNodeRequest -> _state.update { it.copy(isCreatingNode = intent.show) }
                    is GraphIntent.RetryQueuedRequests -> retryQueuedRequests()
                }
            }
        }
    }

    private fun editNodeRequest(id: String?) {
        if (id == null) {
            _state.update { it.copy(editingNode = null) }
        } else {
            val node = _state.value.nodes[id]
            _state.update { it.copy(editingNode = node) }
        }
    }

    private suspend fun updateNodeData(id: String, title: String, description: String) {
        val node = _state.value.nodes[id] ?: return
        val updatedNode = node.copy(title = title, description = description, updatedAt = System.currentTimeMillis())
        repository.updateNode(updatedNode)
        _state.update { it.copy(editingNode = null) } // Close dialog
    }

    private fun selectNode(id: String?) {
        _state.update { it.copy(selectedNodeId = id) }
        if (id != null) {
            hapticEngine.performLightTick()
        }
    }

    private val gson = Gson()

    private fun submitPrompt(prompt: String) {
        val userMsg = ChatMessage(text = prompt, isFromUser = true)
        _state.update {
            it.copy(
                chatHistory = it.chatHistory + userMsg,
                isAiThinking = true
            )
        }

        viewModelScope.launch {
            executeAiRequest(prompt, _state.value.selectedNodeId)
        }
    }

    private fun retryQueuedRequests() {
        val queued = _state.value.queuedRequests
        if (queued.isEmpty()) return

        _state.update { it.copy(isAiThinking = true) }
        viewModelScope.launch {
            queued.forEach { request ->
                executeAiRequest(request.prompt, request.contextNodeId, request.id)
            }
            _state.update { it.copy(isAiThinking = false) }
        }
    }

    private suspend fun executeAiRequest(prompt: String, contextNodeId: String?, queuedId: String? = null) {
        val currentState = _state.value
        val contextPrefix = if (contextNodeId != null) {
            val selectedNode = currentState.nodes[contextNodeId]
            "Context: Expanding on node '${selectedNode?.title}' (ID: ${selectedNode?.id}). "
        } else {
            "Context: Global graph instruction. "
        }

        val systemInstruction = """
            You are the OmniMap Architect. Your goal is to help the user build their project mind-map.
            You must respond with a JSON object containing:
            - "nodes": a list of new nodes to create.
            - "edges": a list of new edges to create.
            - "message": a brief explanation of what you did.
            
            Node types: PROJECT, TASK, IDEA, GOAL, AGENT.
            Edge types: PARENT_OF, DEPENDS_ON, RELATED_TO.
            
            Ensure the response is valid JSON.
        """.trimIndent()

        val fullPrompt = "$systemInstruction\n\n$contextPrefix\nUser prompt: $prompt"
        val result = aiRepository.generateNodeSuggestion(fullPrompt)
        
        val rawText = result.getOrElse { 
            // Handle failure by queueing
            if (queuedId == null) {
                repository.insertQueuedAiRequest(
                    QueuedAiRequest(prompt = prompt, contextNodeId = contextNodeId)
                )
            }
            
            val aiMsg = ChatMessage(text = "AI request failed. ${it.message}. It has been queued for retry.", isFromUser = false)
            _state.update {
                it.copy(
                    chatHistory = it.chatHistory + aiMsg,
                    isAiThinking = if (queuedId != null) it.isAiThinking else false
                )
            }
            return
        }
        
        // Try to parse JSON from the response
        val mutation = try {
            // Find JSON block if AI wrapped it in markdown
            val jsonRegex = Regex("```json\\s*([\\s\\S]*?)\\s*```|(\\{[\\s\\S]*\\})")
            val match = jsonRegex.find(rawText)
            val jsonString = match?.groupValues?.get(1)?.takeIf { it.isNotBlank() } 
                ?: match?.groupValues?.get(2)?.takeIf { it.isNotBlank() }
                ?: rawText
            
            gson.fromJson(jsonString, AiMutationResponse::class.java)
        } catch (e: Exception) {
            null
        }

        val displayText = mutation?.message ?: rawText
        val aiMsg = ChatMessage(text = displayText, isFromUser = false)

        _state.update {
            it.copy(
                chatHistory = it.chatHistory + aiMsg,
                isAiThinking = if (queuedId != null) it.isAiThinking else false
            )
        }

        // Apply mutations if parsed successfully
        mutation?.let {
            it.nodes.forEach { nodeDto ->
                val newNode = Node(
                    type = nodeDto.type,
                    title = nodeDto.title,
                    description = nodeDto.description,
                    data = "{}",
                    x = nodeDto.x ?: (Math.random() * 500).toFloat(),
                    y = nodeDto.y ?: (Math.random() * 500).toFloat()
                )
                repository.insertNode(newNode)
            }
            it.edges.forEach { edgeDto ->
                val newEdge = Edge(
                    sourceId = edgeDto.sourceId,
                    targetId = edgeDto.targetId,
                    type = edgeDto.type
                )
                repository.insertEdge(newEdge)
            }
        }

        // If it was a queued request, remove it from persistence
        queuedId?.let { repository.deleteQueuedAiRequest(it) }

        if (queuedId == null) {
            _state.update { it.copy(isAiThinking = false) }
        }
        
        hapticEngine.performHeavySnap()
    }

    private fun observeGraphData() {
        viewModelScope.launch {
            combine(
                repository.getAllNodes(),
                repository.getAllEdges(),
                repository.getQueuedAiRequests()
            ) { nodesList, edgesList, queuedList ->
                val nodesMap = nodesList.associateBy { it.id }
                _state.update { currentState ->
                    currentState.copy(
                        nodes = nodesMap,
                        edges = edgesList,
                        queuedRequests = queuedList,
                        isLoading = false,
                        error = null
                    )
                }
            }.collect {}
        }
    }

    private suspend fun updateNodePosition(id: String, newX: Float, newY: Float) {
        val node = _state.value.nodes[id] ?: return
        val updatedNode = node.copy(x = newX, y = newY, updatedAt = System.currentTimeMillis())
        
        _state.update { currentState ->
            val newNodes = currentState.nodes.toMutableMap()
            newNodes[id] = updatedNode
            currentState.copy(nodes = newNodes)
        }

        repository.updateNode(updatedNode)
    }

    private suspend fun createNode(intent: GraphIntent.OnNodeCreated) {
        val newNode = Node(
            type = intent.type,
            title = intent.title,
            description = intent.description,
            data = "{}",
            x = intent.x,
            y = intent.y
        )
        repository.insertNode(newNode)
    }

    private suspend fun createEdge(intent: GraphIntent.OnEdgeCreated) {
        val newEdge = Edge(
            sourceId = intent.sourceId,
            targetId = intent.targetId,
            type = intent.type
        )
        repository.insertEdge(newEdge)
        hapticEngine.performHeavySnap()
    }

    private suspend fun deleteNode(id: String) {
        val node = _state.value.nodes[id] ?: return
        repository.deleteNode(node)
    }

    private suspend fun deleteEdge(id: String) {
        val edge = _state.value.edges.find { it.id == id } ?: return
        repository.deleteEdge(edge)
    }
}