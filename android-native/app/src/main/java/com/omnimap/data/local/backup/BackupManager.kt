package com.omnimap.data.local.backup

import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.EdgeType
import com.omnimap.domain.model.Node
import com.omnimap.domain.model.NodeType
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    fun exportToJson(nodes: List<Node>, edges: List<Edge>): String {
        val root = JSONObject()
        val nodesArray = JSONArray()
        for (node in nodes) {
            val nodeObj = JSONObject().apply {
                put("id", node.id)
                put("type", node.type.name)
                put("title", node.title)
                put("description", node.description)
                put("data", node.data)
                put("createdAt", node.createdAt)
                put("updatedAt", node.updatedAt)
                put("x", node.x.toDouble())
                put("y", node.y.toDouble())
                put("isLocked", node.isLocked)
            }
            nodesArray.put(nodeObj)
        }

        val edgesArray = JSONArray()
        for (edge in edges) {
            val edgeObj = JSONObject().apply {
                put("id", edge.id)
                put("sourceId", edge.sourceId)
                put("targetId", edge.targetId)
                put("type", edge.type.name)
                put("createdAt", edge.createdAt)
            }
            edgesArray.put(edgeObj)
        }

        root.put("nodes", nodesArray)
        root.put("edges", edgesArray)
        return root.toString(4) // Pretty print with 4 spaces indent
    }

    fun importFromJson(jsonString: String): Pair<List<Node>, List<Edge>> {
        val root = JSONObject(jsonString)
        val nodesList = mutableListOf<Node>()
        val edgesList = mutableListOf<Edge>()

        if (root.has("nodes")) {
            val nodesArray = root.getJSONArray("nodes")
            for (i in 0 until nodesArray.length()) {
                val obj = nodesArray.getJSONObject(i)
                val node = Node(
                    id = obj.getString("id"),
                    type = NodeType.valueOf(obj.getString("type")),
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    data = obj.optString("data", "{}"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                    x = obj.optDouble("x", 0.0).toFloat(),
                    y = obj.optDouble("y", 0.0).toFloat(),
                    isLocked = obj.optBoolean("isLocked", false)
                )
                nodesList.add(node)
            }
        }

        if (root.has("edges")) {
            val edgesArray = root.getJSONArray("edges")
            for (i in 0 until edgesArray.length()) {
                val obj = edgesArray.getJSONObject(i)
                val edge = Edge(
                    id = obj.getString("id"),
                    sourceId = obj.getString("sourceId"),
                    targetId = obj.getString("targetId"),
                    type = EdgeType.valueOf(obj.getString("type")),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
                edgesList.add(edge)
            }
        }

        return Pair(nodesList, edgesList)
    }
}