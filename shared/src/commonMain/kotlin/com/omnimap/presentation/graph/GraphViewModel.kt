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

    fun isAiConfigured(): Boolean = aiRepository.isConfigured()

    fun saveAiConfiguration(apiKey: String, model: String, baseUrl: String?) {
        viewModelScope.launch {
            aiRepository.saveConfiguration(apiKey, model, baseUrl)
        }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            if (aiRepository is com.omnimap.data.repository.GeminiRepositoryImpl) {
                aiRepository.saveApiKey(key)
            }
        }
    }

    private val _state = MutableStateFlow(GraphState(isLoading = true))
    val state: StateFlow<GraphState> = _state.asStateFlow()

    private val intentChannel = Channel<GraphIntent>(Channel.UNLIMITED)

    private val velocityMap = mutableMapOf<String, Pair<Float, Float>>()

    init {
        handleIntents()
        observeGraphData()
        observeConnectivity()
        startPhysicsLoop()
        startBackgroundRefinement()
    }

    private fun startBackgroundRefinement() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60000 * 5) // Refine every 5 minutes
                if (aiRepository.isConfigured() && !state.value.isAiThinking) {
                    refineGraphAutonomous()
                }
            }
        }
    }

    private suspend fun refineGraphAutonomous() {
        val currentNodes = state.value.nodes.values.filter { !it.isDraft }
        if (currentNodes.size < 2) return

        val graphSummary = currentNodes.joinToString("\n") { "- ${it.title}: ${it.description}" }
        val prompt = """
            Autonomous Refinement Mode.
            Current Graph Summary:
            $graphSummary
            
            Task: Analyze this graph for missing logical connections or obvious next steps.
            Propose 1-2 new nodes or edges that would add value.
            Output ONLY valid JSON for mutations.
        """.trimIndent()
        
        executeAiRequest(prompt, null)
    }

    private fun generateEmbeddingForNode(node: Node) {
        viewModelScope.launch {
            val text = "${node.title} ${node.description}"
            aiRepository.generateEmbedding(text).onSuccess { vector ->
                repository.updateNode(node.copy(embedding = gson.toJson(vector)))
            }
        }
    }

    private fun startPhysicsLoop() {
        viewModelScope.launch {
            while (true) {
                applyPhysics()
                kotlinx.coroutines.delay(16) // ~60fps for physics updates
            }
        }
    }

    private suspend fun applyPhysics() {
        val currentState = _state.value
        if (currentState.isLoading) return
        
        val nodes = currentState.nodes.values.toList()
        val edges = currentState.edges
        
        val newVelocities = mutableMapOf<String, Pair<Float, Float>>()
        
        // 1. Repulsion between all nodes (Electrostatic-like)
        for (i in nodes.indices) {
            val n1 = nodes[i]
            var fx = 0f
            var fy = 0f
            
            for (j in nodes.indices) {
                if (i == j) continue
                val n2 = nodes[j]
                val dx = n1.x - n2.x
                val dy = n1.y - n2.y
                val distSq = dx * dx + dy * dy + 0.1f
                val force = 5000f / distSq
                fx += (dx / Math.sqrt(distSq.toDouble()).toFloat()) * force
                fy += (dy / Math.sqrt(distSq.toDouble()).toFloat()) * force
            }
            
            // 2. Attraction between connected nodes (Spring-like)
            edges.forEach { edge ->
                if (edge.sourceId == n1.id || edge.targetId == n1.id) {
                    val otherId = if (edge.sourceId == n1.id) edge.targetId else edge.sourceId
                    val other = currentState.nodes[otherId] ?: return@forEach
                    val dx = other.x - n1.x
                    val dy = other.y - n1.y
                    val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat() + 0.1f
                    val force = (dist - 300f) * 0.05f // 300px target distance
                    fx += (dx / dist) * force
                    fy += (dy / dist) * force
                }
            }

            // Apply friction/damping
            val (vx, vy) = velocityMap[n1.id] ?: (0f to 0f)
            val newVx = (vx + fx) * 0.8f
            val newVy = (vy + fy) * 0.8f
            
            if (Math.abs(newVx) > 0.1f || Math.abs(newVy) > 0.1f) {
                newVelocities[n1.id] = newVx to newVy
            }
        }

        // Apply new positions
        newVelocities.forEach { (id, v) ->
            velocityMap[id] = v
            val node = currentState.nodes[id] ?: return@forEach
            if (id != currentState.selectedNodeId) { // Don't move the node user is currently dragging
                val newX = node.x + v.first
                val newY = node.y + v.second
                // Only update locally for smoothness, don't spam DB in physics loop
                _state.update { s ->
                    val m = s.nodes.toMutableMap()
                    m[id] = m[id]!!.copy(x = newX, y = newY)
                    s.copy(nodes = m)
                }
            }
        }
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
                    is GraphIntent.OnCommitDrafts -> commitDrafts()
                    is GraphIntent.OnDiscardDrafts -> discardDrafts()
                    is GraphIntent.OnStartSyncServer -> startSyncServer(intent.port)
                    is GraphIntent.OnSyncRequested -> syncWithHub(intent.targetIp, intent.port)
                }
            }
        }
    }

    private fun startSyncServer(port: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val serverSocket = java.net.ServerSocket(port)
                _state.update { it.copy(syncStatus = "Hub Active: Port $port") }
                while (true) {
                    val client = serverSocket.accept()
                    val json = repository.exportGraphToJson()
                    val response = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ${json.length}\r\n\r\n$json"
                    client.getOutputStream().write(response.toByteArray())
                    client.close()
                }
            } catch (e: Exception) {
                _state.update { it.copy(syncStatus = "Hub Error: ${e.message}") }
            }
        }
    }

    private fun syncWithHub(ip: String, port: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(syncStatus = "Syncing with $ip...") }
            try {
                val socket = java.net.Socket(ip, port)
                val request = "GET / HTTP/1.1\r\nHost: $ip\r\n\r\n"
                socket.getOutputStream().write(request.toByteArray())
                
                val response = socket.getInputStream().bufferedReader().readText()
                val json = response.substringAfter("\r\n\r\n")
                
                repository.importGraphFromJson(json)
                _state.update { it.copy(syncStatus = "Sync Complete") }
                hapticEngine.performHeavySnap()
            } catch (e: Exception) {
                _state.update { it.copy(syncStatus = "Sync Failed: ${e.message}") }
            }
        }
    }

    private suspend fun commitDrafts() {
        _state.value.nodes.values.filter { it.isDraft }.forEach {
            repository.updateNode(it.copy(isDraft = false))
        }
        _state.value.edges.filter { it.isDraft }.forEach {
            repository.updateEdge(it.copy(isDraft = false))
        }
        hapticEngine.performHeavySnap()
    }

    private suspend fun discardDrafts() {
        _state.value.nodes.values.filter { it.isDraft }.forEach {
            repository.deleteNode(it)
        }
        _state.value.edges.filter { it.isDraft }.forEach {
            repository.deleteEdge(it)
        }
        hapticEngine.performLightTick()
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
        generateEmbeddingForNode(updatedNode)
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
                    y = nodeDto.y ?: (Math.random() * 500).toFloat(),
                    isDraft = true
                )
                repository.insertNode(newNode)
            }
            it.edges.forEach { edgeDto ->
                val newEdge = Edge(
                    sourceId = edgeDto.sourceId,
                    targetId = edgeDto.targetId,
                    type = edgeDto.type,
                    isDraft = true
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
        generateEmbeddingForNode(newNode)
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