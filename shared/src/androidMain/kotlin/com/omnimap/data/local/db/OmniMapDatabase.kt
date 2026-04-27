package com.omnimap.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.omnimap.data.local.dao.EdgeDao
import com.omnimap.data.local.dao.NodeDao
import com.omnimap.data.local.dao.QueuedAiRequestDao
import com.omnimap.data.local.entity.NodeFts
import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.Node
import com.omnimap.domain.model.QueuedAiRequest

@Database(entities = [Node::class, Edge::class, NodeFts::class, QueuedAiRequest::class], version = 3, exportSchema = false)
abstract class OmniMapDatabase : RoomDatabase() {
    abstract fun nodeDao(): NodeDao
    abstract fun edgeDao(): EdgeDao
    abstract fun queuedAiRequestDao(): QueuedAiRequestDao
}
