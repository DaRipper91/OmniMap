package com.omnimap.domain.repository

import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.Node
import kotlinx.coroutines.flow.Flow

interface OmniMapRepository {
    fun getAllNodes(): Flow<List<Node>>
    fun getNodeById(id: String): Flow<Node?>
    fun searchNodes(query: String): Flow<List<Node>>
    suspend fun getAllNodesSync(): List<Node>
    suspend fun insertNodes(nodes: List<Node>)
    suspend fun insertNode(node: Node)
    suspend fun updateNode(node: Node)
    suspend fun deleteNode(node: Node)

    fun getAllEdges(): Flow<List<Edge>>
    fun getEdgesForNode(nodeId: String): Flow<List<Edge>>
    suspend fun getAllEdgesSync(): List<Edge>
    suspend fun insertEdges(edges: List<Edge>)
    suspend fun insertEdge(edge: Edge)
    suspend fun updateEdge(edge: Edge)
    suspend fun deleteEdge(edge: Edge)
}