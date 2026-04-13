package com.omnimap.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.omnimap.domain.model.Node
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeDao {
    @Query("SELECT * FROM nodes")
    fun getAllNodes(): Flow<List<Node>>

    @Query("SELECT * FROM nodes WHERE id = :id")
    fun getNodeById(id: String): Flow<Node?>

    @Query("""
        SELECT nodes.* FROM nodes
        JOIN nodes_fts ON nodes.id = nodes_fts.rowid
        WHERE nodes_fts MATCH :searchQuery
    """)
    fun searchNodes(searchQuery: String): Flow<List<Node>>

    @Query("SELECT * FROM nodes")
    suspend fun getAllNodesSync(): List<Node>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: Node)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<Node>)

    @Update
    suspend fun updateNode(node: Node)

    @Delete
    suspend fun deleteNode(node: Node)
}