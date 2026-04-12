package com.omnimap.presentation.graph

import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.Node
import com.omnimap.presentation.graph.chat.ChatMessage

data class GraphState(
    val nodes: Map<String, Node> = emptyMap(),
    val edges: List<Edge> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedNodeId: String? = null,
    val isAiThinking: Boolean = false,
    val chatHistory: List<ChatMessage> = emptyList(),
    val editingNode: Node? = null
)