package com.omnimap.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class EdgeType {
    PARENT_OF, DEPENDS_ON, RELATED_TO
}

@Entity(tableName = "edges")
data class Edge(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sourceId: String,
    val targetId: String,
    val type: EdgeType,
    val createdAt: Long = System.currentTimeMillis(),
    val isDraft: Boolean = false
)
