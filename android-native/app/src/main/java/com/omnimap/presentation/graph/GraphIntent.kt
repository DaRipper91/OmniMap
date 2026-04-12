package com.omnimap.presentation.graph

import com.omnimap.domain.model.EdgeType
import com.omnimap.domain.model.NodeType

sealed class GraphIntent {
    object LoadGraph : GraphIntent()
    data class OnNodeDragged(val id: String, val newX: Float, val newY: Float) : GraphIntent()
    data class OnNodeCreated(val type: NodeType, val title: String, val description: String, val x: Float, val y: Float) : GraphIntent()
    data class OnEdgeCreated(val sourceId: String, val targetId: String, val type: EdgeType) : GraphIntent()
    data class OnNodeDeleted(val id: String) : GraphIntent()
    data class OnEdgeDeleted(val id: String) : GraphIntent()
    data class OnNodeSelected(val id: String?) : GraphIntent()
    data class OnSubmitPrompt(val prompt: String) : GraphIntent()
}