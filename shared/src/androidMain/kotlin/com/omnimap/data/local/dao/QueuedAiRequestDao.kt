package com.omnimap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omnimap.domain.model.QueuedAiRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface QueuedAiRequestDao {
    @Query("SELECT * FROM queued_ai_requests ORDER BY timestamp ASC")
    fun getAllQueuedRequests(): Flow<List<QueuedAiRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: QueuedAiRequest)

    @Query("DELETE FROM queued_ai_requests WHERE id = :id")
    suspend fun deleteRequest(id: String)
}
