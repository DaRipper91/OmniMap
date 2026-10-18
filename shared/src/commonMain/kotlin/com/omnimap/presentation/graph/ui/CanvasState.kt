package com.omnimap.presentation.graph.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.omnimap.domain.model.Node

/**
 * Pan/zoom and layout state for the graph canvas, hoisted so it survives tab switches.
 *
 * Coordinate spaces:
 * - Node positions (`Node.x`, `Node.y`) and [nodeSizes] are in **dp**, so a graph looks
 *   the same at any display scale and on phones.
 * - [offset] and [viewport] are in screen **px**. A content point `c` (dp) is drawn at
 *   `c * density * scale + offset`.
 */
@Stable
class CanvasState {
    var scale by mutableStateOf(1f)
    var offset by mutableStateOf(Offset.Zero)
    var viewport by mutableStateOf(IntSize.Zero)
    var density by mutableStateOf(1f)

    /** Measured card sizes in dp, keyed by node id. */
    val nodeSizes = mutableStateMapOf<String, DpSize>()

    /** Set once the canvas has framed the graph, so it doesn't jump on later recompositions. */
    var hasFramedGraph = false

    fun sizeOf(id: String): DpSize = nodeSizes[id] ?: DefaultNodeSize

    /** A node's bounds in content dp. */
    fun boundsOf(node: Node): Rect {
        val s = sizeOf(node.id)
        return Rect(node.x, node.y, node.x + s.width.value, node.y + s.height.value)
    }

    fun screenToContent(p: Offset): Offset = (p - offset) / (scale * density)

    /** The centre of what's on screen, in content dp. */
    fun visibleCenter(): Offset = screenToContent(Offset(viewport.width / 2f, viewport.height / 2f))

    fun nodeAt(contentPoint: Offset, nodes: Collection<Node>, exceptId: String? = null): Node? =
        nodes.lastOrNull { it.id != exceptId && boundsOf(it).contains(contentPoint) }

    fun zoomBy(factor: Float, pivot: Offset = Offset(viewport.width / 2f, viewport.height / 2f)) {
        val newScale = (scale * factor).coerceIn(MIN_SCALE, MAX_SCALE)
        offset = pivot - (pivot - offset) * (newScale / scale)
        scale = newScale
    }

    /** Pan so the content point [c] (dp) is in the middle of the viewport. */
    fun centerOn(c: Offset) {
        offset = Offset(viewport.width / 2f, viewport.height / 2f) - c * (density * scale)
    }

    /** Zoom and pan so every node is visible, with a margin. No-op for an empty graph. */
    fun fitAll(nodes: Collection<Node>) {
        if (nodes.isEmpty() || viewport.width == 0 || viewport.height == 0) return
        val bounds = nodes.map(::boundsOf).reduce { a, b ->
            Rect(minOf(a.left, b.left), minOf(a.top, b.top), maxOf(a.right, b.right), maxOf(a.bottom, b.bottom))
        }
        val marginPx = FIT_MARGIN.value * density
        val fit = minOf(
            (viewport.width - 2 * marginPx) / (bounds.width * density),
            (viewport.height - 2 * marginPx) / (bounds.height * density)
        )
        scale = fit.coerceIn(MIN_SCALE, MAX_FIT_SCALE)
        centerOn(bounds.center)
    }

    companion object {
        const val MIN_SCALE = 0.25f
        const val MAX_SCALE = 3f
        /** Fitting a small graph shouldn't blow it up past this. */
        const val MAX_FIT_SCALE = 1.25f
        val DefaultNodeSize = DpSize(180.dp, 72.dp)
        val FIT_MARGIN = 48.dp
    }
}
