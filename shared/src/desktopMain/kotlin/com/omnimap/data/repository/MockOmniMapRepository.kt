package com.omnimap.data.repository

import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.Node
import com.omnimap.domain.repository.OmniMapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class MockOmniMapRepository : OmniMapRepository {
    private val nodes = MutableStateFlow<Map<String, Node>>(emptyMap())
    private val edges = MutableStateFlow<List<Edge>>(emptyList())

    override fun getAllNodes(): Flow<List<Node>> = nodes.map { it.values.toList() }
    override fun getNodeById(id: String): Flow<Node?> = nodes.map { it[id] }
    override fun searchNodes(query: String): Flow<List<Node>> = nodes.map { it.values.filter { n -> n.title.contains(query, ignoreCase = true) } }
    override suspend fun getAllNodesSync(): List<Node> = nodes.value.values.toList()
    override suspend fun insertNodes(nodesList: List<Node>) {
        val current = nodes.value.toMutableMap()
        nodesList.forEach { current[it.id] = it }
        nodes.value = current
    }
    override suspend fun insertNode(node: Node) {
        val current = nodes.value.toMutableMap()
        current[node.id] = node
        nodes.value = current
    }
    override suspend fun updateNode(node: Node) {
        val current = nodes.value.toMutableMap()
        current[node.id] = node
        nodes.value = current
    }
    override suspend fun deleteNode(node: Node) {
        val current = nodes.value.toMutableMap()
        current.remove(node.id)
        nodes.value = current
    }

    override fun getAllEdges(): Flow<List<Edge>> = edges
    override fun getEdgesForNode(nodeId: String): Flow<List<Edge>> = edges.map { it.filter { e -> e.sourceId == nodeId || e.targetId == nodeId } }
    override suspend fun getAllEdgesSync(): List<Edge> = edges.value
    override suspend fun insertEdges(edgesList: List<Edge>) {
        edges.value = edges.value + edgesList
    }
    override suspend fun insertEdge(edge: Edge) {
        edges.value = edges.value + edge
    }
    override suspend fun updateEdge(edge: Edge) {
        edges.value = edges.value.map { if (it.id == edge.id) edge else it }
    }
    override suspend fun deleteEdge(edge: Edge) {
        edges.value = edges.value.filter { it.id != edge.id }
    }

    override suspend fun exportGraphToJson(): String = "{}"
    override suspend fun importGraphFromJson(json: String): Result<Unit> = Result.success(Unit)
}
