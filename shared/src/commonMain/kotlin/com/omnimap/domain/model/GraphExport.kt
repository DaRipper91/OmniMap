package com.omnimap.domain.model

data class GraphExport(
    val nodes: List<Node>,
    val edges: List<Edge>,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val version: Int = 1
)
