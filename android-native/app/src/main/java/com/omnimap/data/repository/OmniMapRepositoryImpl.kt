package com.omnimap.data.repository

import com.omnimap.data.local.dao.EdgeDao
import com.omnimap.data.local.dao.NodeDao
import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.Node
import com.omnimap.domain.repository.OmniMapRepository
import kotlinx.coroutines.flow.Flow

class OmniMapRepositoryImpl(
    private val nodeDao: NodeDao,
    private val edgeDao: EdgeDao
) : OmniMapRepository {

    override fun getAllNodes(): Flow<List<Node>> {
        return nodeDao.getAllNodes()
    }

    override fun getNodeById(id: String): Flow<Node?> {
        return nodeDao.getNodeById(id)
    }

    override fun searchNodes(query: String): Flow<List<Node>> {
        return nodeDao.searchNodes(query)
    }

    override suspend fun insertNode(node: Node) {
        nodeDao.insertNode(node)
    }

    override suspend fun updateNode(node: Node) {
        nodeDao.updateNode(node)
    }

    override suspend fun deleteNode(node: Node) {
        nodeDao.deleteNode(node)
    }

    override fun getAllEdges(): Flow<List<Edge>> {
        return edgeDao.getAllEdges()
    }

    override fun getEdgesForNode(nodeId: String): Flow<List<Edge>> {
        return edgeDao.getEdgesForNode(nodeId)
    }

    override suspend fun insertEdge(edge: Edge) {
        edgeDao.insertEdge(edge)
    }

    override suspend fun updateEdge(edge: Edge) {
        edgeDao.updateEdge(edge)
    }

    override suspend fun deleteEdge(edge: Edge) {
        edgeDao.deleteEdge(edge)
    }
}