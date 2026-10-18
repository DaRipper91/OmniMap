package com.omnimap.presentation.graph.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.MaterialTheme
import com.omnimap.presentation.graph.GraphIntent
import com.omnimap.presentation.graph.GraphViewModel

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.pow

private const val MIN_SCALE = 0.25f
private const val MAX_SCALE = 3f
private const val SCROLL_PAN_STEP = 40f

/**
 * The graph canvas.
 *
 * Touch: drag to pan, pinch to zoom. Mouse and trackpad: drag the background to pan,
 * scroll to pan, Ctrl/Cmd + scroll to zoom towards the pointer.
 * Keyboard (click the canvas first): N new node, E or Enter edit, Delete/Backspace delete,
 * Esc deselect, Ctrl/Cmd + = / - / 0 zoom in, out, reset.
 */
@Composable
fun OmniMapCanvas(viewModel: GraphViewModel) {
    val state by viewModel.state.collectAsState()

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    val focusRequester = remember { FocusRequester() }

    // Content is drawn with its origin at the top-left, so a screen point p maps to
    // content point (p - offset) / scale. Zooming keeps the point under `pivot` fixed.
    fun zoomBy(factor: Float, pivot: Offset = Offset(viewport.width / 2f, viewport.height / 2f)) {
        val newScale = (scale * factor).coerceIn(MIN_SCALE, MAX_SCALE)
        offset = pivot - (pivot - offset) * (newScale / scale)
        scale = newScale
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        zoomBy(zoomChange)
        offset += offsetChange
    }

    // Base background color darker than the surface
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onSizeChanged { viewport = it }
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                val selected = state.selectedNodeId
                val command = event.isCtrlPressed || event.isMetaPressed
                when {
                    command && (event.key == Key.Equals || event.key == Key.Plus || event.key == Key.NumPadAdd) -> zoomBy(1.2f)
                    command && (event.key == Key.Minus || event.key == Key.NumPadSubtract) -> zoomBy(1 / 1.2f)
                    command && (event.key == Key.Zero || event.key == Key.NumPad0) -> {
                        scale = 1f
                        offset = Offset.Zero
                    }
                    command -> return@onKeyEvent false
                    event.key == Key.N -> viewModel.processIntent(GraphIntent.OnCreateNodeRequest(true))
                    event.key == Key.Escape && selected != null -> viewModel.processIntent(GraphIntent.OnNodeSelected(null))
                    (event.key == Key.E || event.key == Key.Enter) && selected != null ->
                        viewModel.processIntent(GraphIntent.OnEditNodeRequest(selected))
                    (event.key == Key.Delete || event.key == Key.Backspace) && selected != null ->
                        viewModel.processIntent(GraphIntent.OnNodeDeleted(selected))
                    else -> return@onKeyEvent false
                }
                true
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type != PointerEventType.Scroll) continue
                        val change = event.changes.firstOrNull() ?: continue
                        val delta = change.scrollDelta
                        if (event.keyboardModifiers.isCtrlPressed || event.keyboardModifiers.isMetaPressed) {
                            zoomBy(1.1f.pow(-delta.y), change.position)
                        } else {
                            // Shift+wheel arrives as horizontal scroll on most platforms.
                            offset -= Offset(delta.x, delta.y) * SCROLL_PAN_STEP
                        }
                        change.consume()
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // Click background to deselect, and take keyboard focus for shortcuts
                        focusRequester.requestFocus()
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
                    translationY = offset.y,
                    transformOrigin = TransformOrigin(0f, 0f)
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
                        focusRequester.requestFocus()
                        viewModel.processIntent(GraphIntent.OnNodeSelected(id))
                    }
                )
            }
        }
    }
}
