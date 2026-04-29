package com.omnimap.presentation.graph.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material3.MaterialTheme
import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.Node

@Composable
fun EdgeComposable(
    edges: List<Edge>,
    nodes: Map<String, Node>
) {
    val edgeColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = Modifier.fillMaxSize()) {
        edges.forEach { edge ->
            val sourceNode = nodes[edge.sourceId]
            val targetNode = nodes[edge.targetId]

            if (sourceNode != null && targetNode != null) {
                // Better approximation: center nodes assuming standard width/height
                // Node width is min 140dp, max 280dp. Let's use 200dp center.
                // Node height varies, but let's assume ~80dp for center.
                // Note: In a production app, we would use onGloballyPositioned to get exact centers.
                val sourceCenter = Offset(sourceNode.x + 100f, sourceNode.y + 40f)
                val targetCenter = Offset(targetNode.x + 100f, targetNode.y + 40f)

                drawLine(
                    color = edgeColor,
                    start = sourceCenter,
                    end = targetCenter,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round,
                    alpha = 0.6f
                )
            }
        }
    }
}