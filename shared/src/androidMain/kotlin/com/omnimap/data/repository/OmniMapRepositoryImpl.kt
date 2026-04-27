package com.omnimap.data.repository

import com.google.gson.Gson
import com.omnimap.data.local.dao.EdgeDao
import com.omnimap.data.local.dao.NodeDao
import com.omnimap.data.local.dao.QueuedAiRequestDao
import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.GraphExport
import com.omnimap.domain.model.Node
import com.omnimap.domain.model.QueuedAiRequest
import com.omnimap.domain.repository.OmniMapRepository
import kotlinx.coroutines.flow.Flow

class OmniMapRepositoryImpl(
    private val nodeDao: NodeDao,
    private val edgeDao: EdgeDao,
    private val queuedAiRequestDao: QueuedAiRequestDao
) : OmniMapRepository {

    private val gson = Gson()

    override fun getAllNodes(): Flow<List<Node>> = nodeDao.getAllNodes()

    override fun getNodeById(id: String): Flow<Node?> = nodeDao.getNodeById(id)

    override fun searchNodes(query: String): Flow<List<Node>> = nodeDao.searchNodes(query)

    override suspend fun getAllNodesSync(): List<Node> = nodeDao.getAllNodesSync()

    override suspend fun insertNodes(nodes: List<Node>) = nodeDao.insertNodes(nodes)

    override suspend fun insertNode(node: Node) = nodeDao.insertNode(node)

    override suspend fun updateNode(node: Node) = nodeDao.updateNode(node)

    override suspend fun deleteNode(node: Node) = nodeDao.deleteNode(node)

    override fun getAllEdges(): Flow<List<Edge>> = edgeDao.getAllEdges()

    override fun getEdgesForNode(nodeId: String): Flow<List<Edge>> = edgeDao.getEdgesForNode(nodeId)

    override suspend fun getAllEdgesSync(): List<Edge> = edgeDao.getAllEdgesSync()

    override suspend fun insertEdges(edges: List<Edge>) = edgeDao.insertEdges(edges)

    override suspend fun insertEdge(edge: Edge) = edgeDao.insertEdge(edge)

    override suspend fun updateEdge(edge: Edge) = edgeDao.updateEdge(edge)

    override suspend fun deleteEdge(edge: Edge) = edgeDao.deleteEdge(edge)

    override fun getQueuedAiRequests(): Flow<List<QueuedAiRequest>> = queuedAiRequestDao.getAllQueuedRequests()

    override suspend fun insertQueuedAiRequest(request: QueuedAiRequest) = queuedAiRequestDao.insertRequest(request)

    override suspend fun deleteQueuedAiRequest(id: String) = queuedAiRequestDao.deleteRequest(id)

    override suspend fun exportGraphToJson(): String {
        val nodes = nodeDao.getAllNodesSync()
        val edges = edgeDao.getAllEdgesSync()
        val export = GraphExport(nodes, edges)
        return gson.toJson(export)
    }

    override suspend fun importGraphFromJson(json: String): Result<Unit> {
        return try {
            val export = gson.fromJson(json, GraphExport::class.java)
            nodeDao.insertNodes(export.nodes)
            edgeDao.insertEdges(export.edges)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
