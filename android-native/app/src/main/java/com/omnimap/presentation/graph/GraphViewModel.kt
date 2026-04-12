package com.omnimap.presentation.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimap.core.haptics.HapticEngine
import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.Node
import com.omnimap.domain.repository.OmniMapRepository
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
    private val hapticEngine: HapticEngine
) : ViewModel() {

    private val _state = MutableStateFlow(GraphState(isLoading = true))
    val state: StateFlow<GraphState> = _state.asStateFlow()

    private val intentChannel = Channel<GraphIntent>(Channel.UNLIMITED)

    init {
        handleIntents()
        observeGraphData()
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
                        // Initially handled by observeGraphData, but allows manual re-trigger if needed
                    }
                    is GraphIntent.OnNodeDragged -> updateNodePosition(intent.id, intent.newX, intent.newY)
                    is GraphIntent.OnNodeCreated -> createNode(intent)
                    is GraphIntent.OnEdgeCreated -> createEdge(intent)
                    is GraphIntent.OnNodeDeleted -> deleteNode(intent.id)
                    is GraphIntent.OnEdgeDeleted -> deleteEdge(intent.id)
                }
            }
        }
    }

    private fun observeGraphData() {
        viewModelScope.launch {
            combine(
                repository.getAllNodes(),
                repository.getAllEdges()
            ) { nodesList, edgesList ->
                val nodesMap = nodesList.associateBy { it.id }
                _state.update { currentState ->
                    currentState.copy(
                        nodes = nodesMap,
                        edges = edgesList,
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
        
        // Optimistic UI update (immediate visual feedback for 120Hz smooth dragging)
        _state.update { currentState ->
            val newNodes = currentState.nodes.toMutableMap()
            newNodes[id] = updatedNode
            currentState.copy(nodes = newNodes)
        }

        // Persist to database
        repository.updateNode(updatedNode)
    }

    private suspend fun createNode(intent: GraphIntent.OnNodeCreated) {
        val newNode = Node(
            type = intent.type,
            title = intent.title,
            description = intent.description,
            data = "{}", // Initial empty payload
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
    }

    private suspend fun deleteNode(id: String) {
        val node = _state.value.nodes[id] ?: return
        repository.deleteNode(node)
    }

    private suspend fun deleteEdge(id: String) {
        val edge = _state.value.edges.find { it.id == id } ?: return
        repository.deleteEdge(edge)
    }
}.deleteEdge(edge)
    }
}