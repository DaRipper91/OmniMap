package com.omnimap.data.repository

import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.EdgeType
import com.omnimap.domain.model.Node
import com.omnimap.domain.model.NodeType
import com.omnimap.domain.repository.OmniMapRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID

class DesktopOmniMapRepositoryImpl(
    private val dbPath: String = "jdbc:sqlite:omnimap.db"
) : OmniMapRepository {

    private val _nodesFlow = MutableStateFlow<List<Node>>(emptyList())
    private val _edgesFlow = MutableStateFlow<List<Edge>>(emptyList())
    private val _searchFlow = MutableStateFlow<List<Node>>(emptyList())

    init {
        initializeDatabase()
        refreshFlows()
    }

    private fun getConnection(): Connection {
        return DriverManager.getConnection(dbPath)
    }

    private fun initializeDatabase() {
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS nodes (
                        id TEXT PRIMARY KEY,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        data TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        x REAL NOT NULL,
                        y REAL NOT NULL,
                        isLocked INTEGER NOT NULL
                    )
                """.trimIndent())

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS edges (
                        id TEXT PRIMARY KEY,
                        sourceId TEXT NOT NULL,
                        targetId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(sourceId) REFERENCES nodes(id) ON DELETE CASCADE,
                        FOREIGN KEY(targetId) REFERENCES nodes(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                
                // Enable foreign key constraints in SQLite
                stmt.execute("PRAGMA foreign_keys = ON;")
            }
        }
    }

    private fun refreshFlows() {
        _nodesFlow.value = getAllNodesSyncInternal()
        _edgesFlow.value = getAllEdgesSyncInternal()
    }

    override fun getAllNodes(): Flow<List<Node>> = _nodesFlow.asStateFlow()

    private fun getAllNodesSyncInternal(): List<Node> {
        val nodes = mutableListOf<Node>()
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT * FROM nodes")
                while (rs.next()) {
                    nodes.add(
                        Node(
                            id = rs.getString("id"),
                            type = NodeType.valueOf(rs.getString("type")),
                            title = rs.getString("title"),
                            description = rs.getString("description"),
                            data = rs.getString("data"),
                            createdAt = rs.getLong("createdAt"),
                            updatedAt = rs.getLong("updatedAt"),
                            x = rs.getFloat("x"),
                            y = rs.getFloat("y"),
                            isLocked = rs.getInt("isLocked") > 0
                        )
                    )
                }
            }
        }
        return nodes
    }

    override suspend fun getAllNodesSync(): List<Node> = withContext(Dispatchers.IO) {
        getAllNodesSyncInternal()
    }

    override fun getNodeById(id: String): Flow<Node?> {
        val flow = MutableStateFlow<Node?>(null)
        getConnection().use { conn ->
            conn.prepareStatement("SELECT * FROM nodes WHERE id = ?").use { stmt ->
                stmt.setString(1, id)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    flow.value = Node(
                        id = rs.getString("id"),
                        type = NodeType.valueOf(rs.getString("type")),
                        title = rs.getString("title"),
                        description = rs.getString("description"),
                        data = rs.getString("data"),
                        createdAt = rs.getLong("createdAt"),
                        updatedAt = rs.getLong("updatedAt"),
                        x = rs.getFloat("x"),
                        y = rs.getFloat("y"),
                        isLocked = rs.getInt("isLocked") > 0
                    )
                }
            }
        }
        return flow.asStateFlow()
    }

    override fun searchNodes(query: String): Flow<List<Node>> {
        val results = mutableListOf<Node>()
        getConnection().use { conn ->
            conn.prepareStatement("SELECT * FROM nodes WHERE title LIKE ? OR description LIKE ?").use { stmt ->
                val likeQuery = "%$query%"
                stmt.setString(1, likeQuery)
                stmt.setString(2, likeQuery)
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    results.add(
                        Node(
                            id = rs.getString("id"),
                            type = NodeType.valueOf(rs.getString("type")),
                            title = rs.getString("title"),
                            description = rs.getString("description"),
                            data = rs.getString("data"),
                            createdAt = rs.getLong("createdAt"),
                            updatedAt = rs.getLong("updatedAt"),
                            x = rs.getFloat("x"),
                            y = rs.getFloat("y"),
                            isLocked = rs.getInt("isLocked") > 0
                        )
                    )
                }
            }
        }
        _searchFlow.value = results
        return _searchFlow.asStateFlow()
    }

    override suspend fun insertNodes(nodes: List<Node>) = withContext(Dispatchers.IO) {
        getConnection().use { conn ->
            conn.autoCommit = false
            conn.prepareStatement("INSERT OR REPLACE INTO nodes (id, type, title, description, data, createdAt, updatedAt, x, y, isLocked) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)").use { stmt ->
                for (node in nodes) {
                    stmt.setString(1, node.id)
                    stmt.setString(2, node.type.name)
                    stmt.setString(3, node.title)
                    stmt.setString(4, node.description)
                    stmt.setString(5, node.data)
                    stmt.setLong(6, node.createdAt)
                    stmt.setLong(7, node.updatedAt)
                    stmt.setFloat(8, node.x)
                    stmt.setFloat(9, node.y)
                    stmt.setInt(10, if (node.isLocked) 1 else 0)
                    stmt.addBatch()
                }
                stmt.executeBatch()
            }
            conn.commit()
        }
        refreshFlows()
    }

    override suspend fun insertNode(node: Node) = insertNodes(listOf(node))

    override suspend fun updateNode(node: Node) = withContext(Dispatchers.IO) {
        getConnection().use { conn ->
            conn.prepareStatement("UPDATE nodes SET type=?, title=?, description=?, data=?, updatedAt=?, x=?, y=?, isLocked=? WHERE id=?").use { stmt ->
                stmt.setString(1, node.type.name)
                stmt.setString(2, node.title)
                stmt.setString(3, node.description)
                stmt.setString(4, node.data)
                stmt.setLong(5, node.updatedAt)
                stmt.setFloat(6, node.x)
                stmt.setFloat(7, node.y)
                stmt.setInt(8, if (node.isLocked) 1 else 0)
                stmt.setString(9, node.id)
                stmt.executeUpdate()
            }
        }
        refreshFlows()
    }

    override suspend fun deleteNode(node: Node) = withContext(Dispatchers.IO) {
        getConnection().use { conn ->
            conn.prepareStatement("DELETE FROM nodes WHERE id=?").use { stmt ->
                stmt.setString(1, node.id)
                stmt.executeUpdate()
            }
        }
        refreshFlows()
    }

    override fun getAllEdges(): Flow<List<Edge>> = _edgesFlow.asStateFlow()

    private fun getAllEdgesSyncInternal(): List<Edge> {
        val edges = mutableListOf<Edge>()
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT * FROM edges")
                while (rs.next()) {
                    edges.add(
                        Edge(
                            id = rs.getString("id"),
                            sourceId = rs.getString("sourceId"),
                            targetId = rs.getString("targetId"),
                            type = EdgeType.valueOf(rs.getString("type")),
                            createdAt = rs.getLong("createdAt")
                        )
                    )
                }
            }
        }
        return edges
    }

    override suspend fun getAllEdgesSync(): List<Edge> = withContext(Dispatchers.IO) {
        getAllEdgesSyncInternal()
    }

    override fun getEdgesForNode(nodeId: String): Flow<List<Edge>> {
        val flow = MutableStateFlow<List<Edge>>(emptyList())
        val edges = mutableListOf<Edge>()
        getConnection().use { conn ->
            conn.prepareStatement("SELECT * FROM edges WHERE sourceId = ? OR targetId = ?").use { stmt ->
                stmt.setString(1, nodeId)
                stmt.setString(2, nodeId)
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    edges.add(
                        Edge(
                            id = rs.getString("id"),
                            sourceId = rs.getString("sourceId"),
                            targetId = rs.getString("targetId"),
                            type = EdgeType.valueOf(rs.getString("type")),
                            createdAt = rs.getLong("createdAt")
                        )
                    )
                }
            }
        }
        flow.value = edges
        return flow.asStateFlow()
    }

    override suspend fun insertEdges(edges: List<Edge>) = withContext(Dispatchers.IO) {
        getConnection().use { conn ->
            conn.autoCommit = false
            conn.prepareStatement("INSERT OR REPLACE INTO edges (id, sourceId, targetId, type, createdAt) VALUES (?, ?, ?, ?, ?)").use { stmt ->
                for (edge in edges) {
                    stmt.setString(1, edge.id)
                    stmt.setString(2, edge.sourceId)
                    stmt.setString(3, edge.targetId)
                    stmt.setString(4, edge.type.name)
                    stmt.setLong(5, edge.createdAt)
                    stmt.addBatch()
                }
                stmt.executeBatch()
            }
            conn.commit()
        }
        refreshFlows()
    }

    override suspend fun insertEdge(edge: Edge) = insertEdges(listOf(edge))

    override suspend fun updateEdge(edge: Edge) = withContext(Dispatchers.IO) {
        getConnection().use { conn ->
            conn.prepareStatement("UPDATE edges SET sourceId=?, targetId=?, type=? WHERE id=?").use { stmt ->
                stmt.setString(1, edge.sourceId)
                stmt.setString(2, edge.targetId)
                stmt.setString(3, edge.type.name)
                stmt.setString(4, edge.id)
                stmt.executeUpdate()
            }
        }
        refreshFlows()
    }

    override suspend fun deleteEdge(edge: Edge) = withContext(Dispatchers.IO) {
        getConnection().use { conn ->
            conn.prepareStatement("DELETE FROM edges WHERE id=?").use { stmt ->
                stmt.setString(1, edge.id)
                stmt.executeUpdate()
            }
        }
        refreshFlows()
    }
}