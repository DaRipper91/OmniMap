package com.omnimap.presentation.graph.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.Node

@Composable
fun EdgeComposable(
    edges: List<Edge>,
    nodes: Map<String, Node>
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        edges.forEach { edge ->
            val sourceNode = nodes[edge.sourceId]
            val targetNode = nodes[edge.targetId]

            if (sourceNode != null && targetNode != null) {
                // Approximate center of the node (assuming ~200px width, ~100px height for now)
                val sourceOffset = Offset(sourceNode.x + 200f, sourceNode.y + 100f)
                val targetOffset = Offset(targetNode.x + 200f, targetNode.y + 100f)

                drawLine(
                    color = Color(0xFF49454F), // MD3 Outline color variant
                    start = sourceOffset,
                    end = targetOffset,
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}