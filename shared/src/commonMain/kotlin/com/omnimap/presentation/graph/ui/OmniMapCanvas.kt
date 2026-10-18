package com.omnimap.presentation.graph.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.omnimap.domain.model.EdgeType
import com.omnimap.presentation.graph.GraphIntent
import com.omnimap.presentation.graph.GraphViewModel
import kotlin.math.pow

private const val SCROLL_PAN_STEP = 40f
private val GridSpacing = 24.dp

/**
 * The graph canvas.
 *
 * Touch: drag to pan, pinch to zoom. Mouse and trackpad: drag the background to pan,
 * scroll to pan, Ctrl/Cmd + scroll to zoom towards the pointer.
 * Keyboard (click the canvas first): N new node, L link selected node, E or Enter edit,
 * Delete/Backspace delete, Esc deselect, F or Ctrl/Cmd+0 fit all, Ctrl/Cmd + = / - zoom.
 *
 * Linking: drag the handle on the selected node's right edge onto another node;
 * [onLinkDropped] is called with the source and target ids.
 */
@Composable
fun OmniMapCanvas(
    viewModel: GraphViewModel,
    canvasState: CanvasState,
    onLinkDropped: (sourceId: String, targetId: String) -> Unit = { s, t ->
        viewModel.processIntent(GraphIntent.OnEdgeCreated(s, t, EdgeType.RELATED_TO))
    },
    onLinkRequest: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    var linkDrag by remember { mutableStateOf<LinkDrag?>(null) }
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)

    canvasState.density = LocalDensity.current.density

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        canvasState.zoomBy(zoomChange)
        canvasState.offset += offsetChange
    }

    // Frame the graph the first time it's shown, or centre on a node opened from Search.
    LaunchedEffect(state.nodes.isNotEmpty(), canvasState.viewport, state.focusRequest) {
        if (canvasState.viewport.width == 0 || state.nodes.isEmpty()) return@LaunchedEffect
        withFrameNanos { } // let cards measure first
        val focus = state.focusNodeId?.let { state.nodes[it] }
        if (focus != null) {
            canvasState.centerOn(canvasState.boundsOf(focus).center)
            canvasState.hasFramedGraph = true
            viewModel.processIntent(GraphIntent.OnFocusHandled)
        } else if (!canvasState.hasFramedGraph) {
            canvasState.fitAll(state.nodes.values)
            canvasState.hasFramedGraph = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Dot grid that moves and scales with the canvas, so panning has a visible reference.
            .drawBehind {
                var step = GridSpacing.toPx() * canvasState.scale
                while (step < 12f) step *= 2
                val radius = 1.2.dp.toPx()
                val startX = ((canvasState.offset.x % step) + step) % step
                val startY = ((canvasState.offset.y % step) + step) % step
                var x = startX
                while (x < size.width) {
                    var y = startY
                    while (y < size.height) {
                        drawCircle(gridColor, radius, Offset(x, y))
                        y += step
                    }
                    x += step
                }
            }
            .onSizeChanged { canvasState.viewport = it }
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                val selected = state.selectedNodeId
                val command = event.isCtrlPressed || event.isMetaPressed
                when {
                    command && (event.key == Key.Equals || event.key == Key.Plus || event.key == Key.NumPadAdd) -> canvasState.zoomBy(1.2f)
                    command && (event.key == Key.Minus || event.key == Key.NumPadSubtract) -> canvasState.zoomBy(1 / 1.2f)
                    command && (event.key == Key.Zero || event.key == Key.NumPad0) -> canvasState.fitAll(state.nodes.values)
                    command -> return@onKeyEvent false
                    event.key == Key.F -> canvasState.fitAll(state.nodes.values)
                    event.key == Key.N -> viewModel.processIntent(GraphIntent.OnCreateNodeRequest(true))
                    event.key == Key.L && selected != null -> onLinkRequest()
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
                            canvasState.zoomBy(1.1f.pow(-delta.y), change.position)
                        } else {
                            // Shift+wheel arrives as horizontal scroll on most platforms.
                            canvasState.offset -= Offset(delta.x, delta.y) * SCROLL_PAN_STEP
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
                    scaleX = canvasState.scale,
                    scaleY = canvasState.scale,
                    translationX = canvasState.offset.x,
                    translationY = canvasState.offset.y,
                    transformOrigin = TransformOrigin(0f, 0f)
                )
        ) {
            // 1. Draw edges below nodes
            EdgeComposable(
                edges = state.edges,
                nodes = state.nodes,
                canvasState = canvasState,
                selectedNodeId = state.selectedNodeId,
                linkDrag = linkDrag
            )

            // 2. Draw nodes on top
            state.nodes.values.forEach { node ->
                NodeComposable(
                    node = node,
                    isSelected = state.selectedNodeId == node.id,
                    canvasState = canvasState,
                    onNodeDragged = { id, newX, newY ->
                        viewModel.processIntent(GraphIntent.OnNodeDragged(id, newX, newY))
                    },
                    onNodeSelected = { id ->
                        focusRequester.requestFocus()
                        viewModel.processIntent(GraphIntent.OnNodeSelected(id))
                    },
                    onLinkDragStart = { id ->
                        val bounds = canvasState.boundsOf(node)
                        linkDrag = LinkDrag(id, Offset(bounds.right + 20f, bounds.center.y))
                    },
                    onLinkDrag = { delta ->
                        linkDrag = linkDrag?.let { it.copy(pointer = it.pointer + delta) }
                    },
                    onLinkDragEnd = {
                        linkDrag?.let { drag ->
                            canvasState.nodeAt(drag.pointer, state.nodes.values, exceptId = drag.sourceId)
                                ?.let { target -> onLinkDropped(drag.sourceId, target.id) }
                        }
                        linkDrag = null
                    }
                )
            }
        }
    }
}
