package com.omnimap.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class NodeType {
    PROJECT, TASK, IDEA, GOAL, AGENT
}

@Entity(tableName = "nodes")
data class Node(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: NodeType,
    val title: String,
    val description: String,
    val data: String, // JSON payload for flexible data
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val x: Float = 0f,
    val y: Float = 0f,
    val isLocked: Boolean = false,
    val isDraft: Boolean = false,
    val embedding: String? = null // JSON array of floats
)
