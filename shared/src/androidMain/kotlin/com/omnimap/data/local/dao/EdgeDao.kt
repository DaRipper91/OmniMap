package com.omnimap.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.omnimap.domain.model.Edge
import kotlinx.coroutines.flow.Flow

@Dao
interface EdgeDao {
    @Query("SELECT * FROM edges")
    fun getAllEdges(): Flow<List<Edge>>

    @Query("SELECT * FROM edges WHERE sourceId = :nodeId OR targetId = :nodeId")
    fun getEdgesForNode(nodeId: String): Flow<List<Edge>>

    @Query("SELECT * FROM edges")
    suspend fun getAllEdgesSync(): List<Edge>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEdge(edge: Edge)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEdges(edges: List<Edge>)

    @Update
    suspend fun updateEdge(edge: Edge)

    @Delete
    suspend fun deleteEdge(edge: Edge)
}