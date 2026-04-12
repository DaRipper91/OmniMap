package com.omnimap.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class EdgeType {
    PARENT_OF, DEPENDS_ON, RELATED_TO, CREATED_BY
}

@Entity(
    tableName = "edges",
    foreignKeys = [
        ForeignKey(
            entity = Node::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Node::class,
            parentColumns = ["id"],
            childColumns = ["targetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sourceId"), Index("targetId")]
)
data class Edge(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sourceId: String,
    val targetId: String,
    val type: EdgeType,
    val createdAt: Long = System.currentTimeMillis()
)
