package com.omnimap.data.remote.dto

import com.omnimap.domain.model.EdgeType
import com.omnimap.domain.model.NodeType

data class AiMutationResponse(
    val nodes: List<AiNodeDto> = emptyList(),
    val edges: List<AiEdgeDto> = emptyList(),
    val message: String? = null
)

data class AiNodeDto(
    val type: NodeType,
    val title: String,
    val description: String = "",
    val x: Float? = null,
    val y: Float? = null
)

data class AiEdgeDto(
    val sourceId: String,
    val targetId: String,
    val type: EdgeType
)
