package com.omnimap.presentation.graph.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.omnimap.presentation.graph.GraphIntent
import com.omnimap.presentation.graph.GraphViewModel

@Composable
fun OmniMapCanvas(viewModel: GraphViewModel) {
    val state by viewModel.state.collectAsState()

    // Base background color darker than the surface
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141218)) // MD3 Background Dark
    ) {
        // 1. Draw edges below nodes
        EdgeComposable(edges = state.edges, nodes = state.nodes)

        // 2. Draw nodes on top
        state.nodes.values.forEach { node ->
            NodeComposable(
                node = node,
                onNodeDragged = { id, newX, newY ->
                    viewModel.processIntent(GraphIntent.OnNodeDragged(id, newX, newY))
                }
            )
        }
    }
}