package com.omnimap.presentation.graph.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.EdgeType
import com.omnimap.domain.model.Node
import kotlin.math.abs
import kotlin.math.hypot

/** An in-progress link drag: from [sourceId] to a point in content dp. */
data class LinkDrag(val sourceId: String, val pointer: Offset)

/**
 * Draws edges between the measured borders of their nodes, so lines meet the cards
 * at any zoom or display scale.
 *
 * - Parent of: solid line with an arrowhead at the child.
 * - Depends on: dashed line with an arrowhead at the dependency.
 * - Related to: dotted line, no arrowhead.
 * - AI drafts: rose instead of grey, and dashed if the type would be solid.
 * - Edges touching the selected node are drawn in the primary colour.
 */
@Composable
fun EdgeComposable(
    edges: List<Edge>,
    nodes: Map<String, Node>,
    canvasState: CanvasState,
    selectedNodeId: String?,
    linkDrag: LinkDrag?
) {
    val committed = MaterialTheme.colorScheme.outline
    val draft = MaterialTheme.colorScheme.tertiary
    val highlight = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val px = density
        fun Rect.toPx() = Rect(left * px, top * px, right * px, bottom * px)

        edges.forEach { edge ->
            val source = nodes[edge.sourceId] ?: return@forEach
            val target = nodes[edge.targetId] ?: return@forEach
            val a = canvasState.boundsOf(source).toPx()
            val b = canvasState.boundsOf(target).toPx()
            val (start, end) = borderToBorder(a, b, gap = 4.dp.toPx()) ?: return@forEach

            val isDraft = edge.isDraft || source.isDraft || target.isDraft
            val touchesSelection = selectedNodeId != null && (edge.sourceId == selectedNodeId || edge.targetId == selectedNodeId)
            val color = when {
                isDraft -> draft
                touchesSelection -> highlight
                else -> committed
            }
            val dash = when (edge.type) {
                EdgeType.PARENT_OF -> if (isDraft) floatArrayOf(10.dp.toPx(), 6.dp.toPx()) else null
                EdgeType.DEPENDS_ON -> floatArrayOf(10.dp.toPx(), 6.dp.toPx())
                EdgeType.RELATED_TO -> floatArrayOf(1.dp.toPx(), 6.dp.toPx())
            }
            val hasArrow = edge.type != EdgeType.RELATED_TO
            val arrowLength = 10.dp.toPx()
            // Stop the line at the arrow's base so the stroke doesn't poke through its tip.
            val lineEnd = if (hasArrow) end - (end - start).normalized() * (arrowLength * 0.8f) else end

            drawLine(
                color = color,
                start = start,
                end = lineEnd,
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = dash?.let { PathEffect.dashPathEffect(it) }
            )
            if (hasArrow) drawArrowhead(start, end, color, arrowLength)
        }

        // Preview line while dragging a new link from the selected node's handle.
        linkDrag?.let { drag ->
            val source = nodes[drag.sourceId] ?: return@let
            val from = canvasState.boundsOf(source).toPx().center
            val to = drag.pointer * px
            drawLine(
                color = highlight,
                start = from,
                end = to,
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))
            )
            drawCircle(highlight, radius = 6.dp.toPx(), center = to)
        }
    }
}

private fun Offset.normalized(): Offset {
    val len = hypot(x, y)
    return if (len == 0f) Offset.Zero else this / len
}

/**
 * The segment between the centres of [a] and [b], trimmed to where it leaves each
 * rectangle (plus [gap]). Null when the rectangles overlap and there's nothing to draw.
 */
private fun borderToBorder(a: Rect, b: Rect, gap: Float): Pair<Offset, Offset>? {
    val ca = a.center
    val cb = b.center
    val v = cb - ca
    if (v.x == 0f && v.y == 0f) return null
    fun exitT(r: Rect): Float {
        val tx = if (v.x != 0f) (r.width / 2) / abs(v.x) else Float.MAX_VALUE
        val ty = if (v.y != 0f) (r.height / 2) / abs(v.y) else Float.MAX_VALUE
        return minOf(tx, ty)
    }
    val len = hypot(v.x, v.y)
    val ta = exitT(a) + gap / len
    val tb = exitT(b) + gap / len
    if (ta + tb >= 1f) return null
    return (ca + v * ta) to (cb - v * tb)
}

private fun DrawScope.drawArrowhead(from: Offset, tip: Offset, color: Color, length: Float) {
    val dir = (tip - from).normalized()
    val normal = Offset(-dir.y, dir.x)
    val base = tip - dir * length
    val halfWidth = length * 0.55f
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(base.x + normal.x * halfWidth, base.y + normal.y * halfWidth)
        lineTo(base.x - normal.x * halfWidth, base.y - normal.y * halfWidth)
        close()
    }
    drawPath(path, color)
}
