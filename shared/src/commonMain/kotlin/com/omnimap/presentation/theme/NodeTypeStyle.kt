package com.omnimap.presentation.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.omnimap.domain.model.NodeType

/** Accent colour for each node type, in the variant that reads on the current theme. */
val NodeType.accent: Color
    @Composable get() {
        val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        return when (this) {
            NodeType.PROJECT -> if (dark) NodeProject else NodeProjectLight
            NodeType.TASK -> if (dark) NodeTask else NodeTaskLight
            NodeType.IDEA -> if (dark) NodeIdea else NodeIdeaLight
            NodeType.GOAL -> if (dark) NodeGoal else NodeGoalLight
            NodeType.AGENT -> if (dark) NodeAgent else NodeAgentLight
        }
    }

val NodeType.label: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }

/**
 * The shape that identifies a node type: Project rounded square, Task square,
 * Idea diamond, Goal ring, Agent hexagon.
 */
@Composable
fun NodeTypeGlyph(type: NodeType, modifier: Modifier = Modifier, size: Dp = 10.dp, color: Color = type.accent) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        when (type) {
            NodeType.PROJECT -> drawRoundRect(color, cornerRadius = CornerRadius(w * 0.3f))
            NodeType.TASK -> drawRect(color)
            NodeType.IDEA -> drawPath(Path().apply {
                moveTo(w / 2, 0f); lineTo(w, w / 2); lineTo(w / 2, w); lineTo(0f, w / 2); close()
            }, color)
            NodeType.GOAL -> drawCircle(color, radius = w * 0.36f, style = Stroke(width = w * 0.25f))
            NodeType.AGENT -> drawPath(Path().apply {
                moveTo(w * 0.25f, w * 0.07f); lineTo(w * 0.75f, w * 0.07f); lineTo(w, w / 2)
                lineTo(w * 0.75f, w * 0.93f); lineTo(w * 0.25f, w * 0.93f); lineTo(0f, w / 2); close()
            }, color)
        }
    }
}
