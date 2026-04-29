package com.omnimap.presentation.graph.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.MaterialTheme
import com.omnimap.presentation.graph.GraphIntent
import com.omnimap.presentation.graph.GraphViewModel

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun OmniMapCanvas(viewModel: GraphViewModel) {
    val state by viewModel.state.collectAsState()

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    // Base background color darker than the surface
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // Click background to deselect
                        viewModel.processIntent(GraphIntent.OnNodeSelected(null))
                    }
                )
            }
            .transformable(state = transformState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            // 1. Draw edges below nodes
            EdgeComposable(edges = state.edges, nodes = state.nodes)

            // 2. Draw nodes on top
            state.nodes.values.forEach { node ->
                NodeComposable(
                    node = node,
                    isSelected = state.selectedNodeId == node.id,
                    onNodeDragged = { id, newX, newY ->
                        viewModel.processIntent(GraphIntent.OnNodeDragged(id, newX, newY))
                    },
                    onNodeSelected = { id ->
                        viewModel.processIntent(GraphIntent.OnNodeSelected(id))
                    }
                )
            }
        }
    }
}