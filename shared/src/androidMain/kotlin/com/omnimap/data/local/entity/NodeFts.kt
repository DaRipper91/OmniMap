package com.omnimap.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import com.omnimap.domain.model.Node

@Fts4(contentEntity = Node::class)
@Entity(tableName = "nodes_fts")
data class NodeFts(
    val title: String,
    val description: String
)
