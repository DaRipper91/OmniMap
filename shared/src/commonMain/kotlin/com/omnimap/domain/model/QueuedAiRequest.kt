package com.omnimap.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "queued_ai_requests")
data class QueuedAiRequest(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val prompt: String,
    val contextNodeId: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)
