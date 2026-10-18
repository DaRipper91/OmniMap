package com.omnimap.presentation.graph.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.omnimap.domain.model.Node
import com.omnimap.presentation.theme.NodeTypeGlyph
import com.omnimap.presentation.theme.accent
import com.omnimap.presentation.theme.label

private val NodeShape = RoundedCornerShape(28.dp)

/**
 * One node on the canvas. Positioned in dp ([Node.x], [Node.y]); reports its measured
 * size to [canvasState] so edges can anchor to its real border.
 *
 * Drag deltas arrive in content px (this sits inside the canvas's scaled layer) and are
 * converted to dp before being reported.
 */
@Composable
fun NodeComposable(
    node: Node,
    isSelected: Boolean,
    canvasState: CanvasState,
    onNodeDragged: (String, Float, Float) -> Unit,
    onNodeSelected: (String) -> Unit,
    onLinkDragStart: (String) -> Unit = {},
    onLinkDrag: (Offset) -> Unit = {},
    onLinkDragEnd: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val accent = node.type.accent
    val tertiary = MaterialTheme.colorScheme.tertiary

    Row(modifier = Modifier.offset(node.x.dp, node.y.dp), verticalAlignment = Alignment.CenterVertically) {
        Card(
            modifier = Modifier
                .onSizeChanged { px ->
                    canvasState.nodeSizes[node.id] = with(density) { DpSize(px.width.toDp(), px.height.toDp()) }
                }
                .pointerInput(node.id) {
                    detectTapGestures(onTap = { onNodeSelected(node.id) })
                }
                .pointerInput(node.id) {
                    detectDragGestures(
                        onDragStart = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNodeSelected(node.id)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onNodeDragged(node.id, node.x + dragAmount.x / density.density, node.y + dragAmount.y / density.density)
                        }
                    )
                }
                .then(
                    if (node.isDraft) {
                        // Dashed rose outline: "proposed by the Architect, not yours yet".
                        Modifier.drawWithContent {
                            drawContent()
                            val w = 2.dp.toPx()
                            drawRoundRect(
                                color = tertiary,
                                topLeft = Offset(w / 2, w / 2),
                                size = size.copy(width = size.width - w, height = size.height - w),
                                cornerRadius = CornerRadius(28.dp.toPx()),
                                style = Stroke(width = w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx())))
                            )
                        }
                    } else Modifier
                )
                .widthIn(min = 160.dp, max = 260.dp),
            shape = NodeShape,
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
            border = when {
                node.isDraft -> null
                isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .alpha(if (node.isDraft) 0.65f else 1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NodeTypeGlyph(node.type)
                    Text(
                        text = node.type.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (node.isDraft) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "AI DRAFT",
                            style = MaterialTheme.typography.labelSmall,
                            color = tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = node.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (node.description.isNotBlank()) {
                    Text(
                        text = node.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Link handle: drag from here onto another node to connect them.
        if (isSelected && !node.isDraft) {
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(32.dp)
                    .pointerInput(node.id) {
                        detectDragGestures(
                            onDragStart = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onLinkDragStart(node.id)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onLinkDrag(Offset(dragAmount.x / density.density, dragAmount.y / density.density))
                            },
                            onDragEnd = onLinkDragEnd,
                            onDragCancel = onLinkDragEnd
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                )
            }
        }
    }
}
