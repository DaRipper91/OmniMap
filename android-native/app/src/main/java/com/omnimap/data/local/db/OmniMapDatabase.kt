package com.omnimap.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.omnimap.data.local.dao.EdgeDao
import com.omnimap.data.local.dao.NodeDao
import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.Node

@Database(entities = [Node::class, Edge::class], version = 1, exportSchema = false)
abstract class OmniMapDatabase : RoomDatabase() {
    abstract fun nodeDao(): NodeDao
    abstract fun edgeDao(): EdgeDao
}
