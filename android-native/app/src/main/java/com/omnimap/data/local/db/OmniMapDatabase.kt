package com.omnimap.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.omnimap.domain.model.Node
import com.omnimap.domain.model.Edge

@Database(entities = [Node::class, Edge::class], version = 1, exportSchema = false)
abstract class OmniMapDatabase : RoomDatabase() {
    // DAOs will be defined here next
}
