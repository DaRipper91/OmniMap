package com.omnimap.data.repository

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.omnimap.core.util.DesktopPaths
import com.omnimap.core.util.OmniLogger
import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.GraphExport
import com.omnimap.domain.model.Node
import com.omnimap.domain.model.QueuedAiRequest
import com.omnimap.domain.repository.OmniMapRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * Desktop persistence: the whole graph lives in memory and is written to
 * `$XDG_DATA_HOME/omnimap/graph.json` (`~/.local/share/omnimap/`) a moment
 * after each change, so dragging a node doesn't rewrite the file every frame.
 * Call [flush] on exit to write any pending change immediately.
 */
class FileOmniMapRepository(
    private val file: File = File(DesktopPaths.dataDir(), "graph.json")
) : OmniMapRepository {

    private data class Snapshot(
        val nodes: List<Node> = emptyList(),
        val edges: List<Edge> = emptyList(),
        val queuedRequests: List<QueuedAiRequest> = emptyList(),
        val version: Int = 1
    )

    private val gson: Gson = GsonBuilder().create()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pendingSave: Job? = null

    private val nodes = MutableStateFlow<Map<String, Node>>(emptyMap())
    private val edges = MutableStateFlow<List<Edge>>(emptyList())
    private val queuedRequests = MutableStateFlow<List<QueuedAiRequest>>(emptyList())

    init {
        load()
    }

    private fun load() {
        if (!file.exists()) return
        try {
            val snapshot = gson.fromJson(file.readText(), Snapshot::class.java) ?: return
            nodes.value = snapshot.nodes.orEmpty().associateBy { it.id }
            edges.value = snapshot.edges.orEmpty()
            queuedRequests.value = snapshot.queuedRequests.orEmpty()
        } catch (e: Exception) {
            // Keep the unreadable file for recovery instead of overwriting it on the next save.
            val backup = File(file.parentFile, "graph.corrupt-${System.currentTimeMillis()}.json")
            file.renameTo(backup)
            OmniLogger.e(TAG, "Could not read ${file.path}; moved it to ${backup.name}", e)
        }
    }

    private fun scheduleSave() {
        synchronized(this) {
            pendingSave?.cancel()
            pendingSave = scope.launch {
                delay(SAVE_DELAY_MS)
                writeToDisk()
            }
        }
    }

    /** Writes any pending change now. Safe to call from the UI thread on exit. */
    fun flush() {
        synchronized(this) {
            pendingSave?.cancel()
            pendingSave = null
        }
        writeToDisk()
    }

    private fun writeToDisk() {
        try {
            synchronized(file) {
                // Read the state inside the lock, so a slower save can never write an
                // older snapshot over a newer one.
                val snapshot = Snapshot(nodes.value.values.toList(), edges.value, queuedRequests.value)
                file.parentFile?.mkdirs()
                val tmp = File(file.parentFile, "${file.name}.tmp")
                tmp.writeText(gson.toJson(snapshot))
                DesktopPaths.atomicReplace(tmp, file)
            }
        } catch (e: Exception) {
            OmniLogger.e(TAG, "Could not save graph to ${file.path}", e)
        }
    }

    private inline fun mutateNodes(block: (MutableMap<String, Node>) -> Unit) {
        // update() retries on contention, so concurrent writers on Dispatchers.IO don't drop each other's changes.
        nodes.update { it.toMutableMap().apply(block) }
        scheduleSave()
    }

    private inline fun mutateEdges(block: (List<Edge>) -> List<Edge>) {
        edges.update(block)
        scheduleSave()
    }

    override fun getAllNodes(): Flow<List<Node>> = nodes.map { it.values.toList() }
    override fun getNodeById(id: String): Flow<Node?> = nodes.map { it[id] }
    override fun searchNodes(query: String): Flow<List<Node>> = nodes.map { all ->
        all.values.filter { n ->
            n.title.contains(query, ignoreCase = true) || n.description.contains(query, ignoreCase = true)
        }
    }
    override suspend fun getAllNodesSync(): List<Node> = nodes.value.values.toList()
    override suspend fun insertNodes(nodes: List<Node>) = mutateNodes { m -> nodes.forEach { m[it.id] = it } }
    override suspend fun insertNode(node: Node) = mutateNodes { it[node.id] = node }
    override suspend fun updateNode(node: Node) = mutateNodes { it[node.id] = node }
    override suspend fun deleteNode(node: Node) {
        mutateNodes { it.remove(node.id) }
        // Drop edges to the deleted node so they don't pile up in graph.json.
        mutateEdges { list -> list.filter { it.sourceId != node.id && it.targetId != node.id } }
    }

    override fun getAllEdges(): Flow<List<Edge>> = edges
    override fun getEdgesForNode(nodeId: String): Flow<List<Edge>> =
        edges.map { it.filter { e -> e.sourceId == nodeId || e.targetId == nodeId } }
    override suspend fun getAllEdgesSync(): List<Edge> = edges.value
    override suspend fun insertEdges(edges: List<Edge>) = mutateEdges { current ->
        val incoming = edges.associateBy { it.id }
        current.filter { it.id !in incoming } + edges
    }
    override suspend fun insertEdge(edge: Edge) = insertEdges(listOf(edge))
    override suspend fun updateEdge(edge: Edge) = mutateEdges { list -> list.map { if (it.id == edge.id) edge else it } }
    override suspend fun deleteEdge(edge: Edge) = mutateEdges { list -> list.filter { it.id != edge.id } }

    override fun getQueuedAiRequests(): Flow<List<QueuedAiRequest>> = queuedRequests

    override suspend fun insertQueuedAiRequest(request: QueuedAiRequest) {
        queuedRequests.update { list -> list.filter { it.id != request.id } + request }
        scheduleSave()
    }

    override suspend fun deleteQueuedAiRequest(id: String) {
        queuedRequests.update { list -> list.filter { it.id != id } }
        scheduleSave()
    }

    override suspend fun exportGraphToJson(): String =
        gson.toJson(GraphExport(nodes.value.values.toList(), edges.value))

    override suspend fun importGraphFromJson(json: String): Result<Unit> = try {
        val export = gson.fromJson(json, GraphExport::class.java)
            ?: throw IllegalArgumentException("Empty file")
        insertNodes(export.nodes.orEmpty())
        insertEdges(export.edges.orEmpty())
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private companion object {
        const val TAG = "FileOmniMapRepository"
        const val SAVE_DELAY_MS = 400L
    }
}
