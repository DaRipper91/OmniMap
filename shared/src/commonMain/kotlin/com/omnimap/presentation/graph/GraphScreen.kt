package com.omnimap.presentation.graph

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.omnimap.presentation.graph.chat.ChatContent
import com.omnimap.presentation.graph.chat.IntelligentBottomSheet
import com.omnimap.presentation.graph.ui.CanvasState
import com.omnimap.presentation.graph.ui.CreateNodeDialog
import com.omnimap.presentation.graph.ui.EditNodeDialog
import com.omnimap.presentation.graph.ui.LinkTypeDialog
import com.omnimap.presentation.graph.ui.LinksDialog
import com.omnimap.presentation.graph.ui.OmniMapCanvas
import com.omnimap.presentation.theme.NodeTypeGlyph

@Composable
fun GraphScreen(viewModel: GraphViewModel, canvasState: CanvasState = remember { CanvasState() }, isWide: Boolean = false) {
    if (isWide) WideGraphScreen(viewModel, canvasState) else CompactGraphScreen(viewModel, canvasState)
}

/** Phones: canvas with the Architect in a bottom sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactGraphScreen(viewModel: GraphViewModel, canvasState: CanvasState) {
    val state by viewModel.state.collectAsState()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                IntelligentBottomSheet(viewModel = viewModel, state = state)
            },
            sheetPeekHeight = 64.dp,
            sheetContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            content = { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    GraphCanvasArea(viewModel, state, canvasState, showDraftStrip = true, controlsBottomPadding = 80.dp)
                }
            }
        )
    }
}

/** Laptops and desktops: canvas with the Architect in a collapsible side panel. */
@Composable
private fun WideGraphScreen(viewModel: GraphViewModel, canvasState: CanvasState) {
    val state by viewModel.state.collectAsState()
    var isPanelOpen by remember { mutableStateOf(true) }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            GraphCanvasArea(viewModel, state, canvasState, showDraftStrip = !isPanelOpen, controlsBottomPadding = 16.dp)

            FilledTonalIconButton(
                onClick = { isPanelOpen = !isPanelOpen },
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) {
                Icon(
                    if (isPanelOpen) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = if (isPanelOpen) "Hide Architect panel" else "Show Architect panel"
                )
            }
        }

        if (isPanelOpen) {
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Surface(
                modifier = Modifier.width(380.dp).fillMaxHeight(),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    DraftReviewCard(state, viewModel)
                    ChatContent(
                        state = state,
                        onPromptSubmitted = { prompt -> viewModel.processIntent(GraphIntent.OnSubmitPrompt(prompt)) },
                        onRetryClicked = { viewModel.processIntent(GraphIntent.RetryQueuedRequests) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private val GraphState.draftCount: Int
    get() = nodes.values.count { it.isDraft } + edges.count { it.isDraft }

private fun changesLabel(count: Int) = if (count == 1) "1 proposed change" else "$count proposed changes"

/** Draft actions at the top of the Architect panel (wide layout). */
@Composable
private fun DraftReviewCard(state: GraphState, viewModel: GraphViewModel) {
    val count = state.draftCount
    if (count == 0) return
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(changesLabel(count), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
            Text(
                "Dashed rose nodes and links on the canvas are the Architect's drafts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )
            DraftButtons(viewModel)
        }
    }
}

@Composable
private fun DraftButtons(viewModel: GraphViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { viewModel.processIntent(GraphIntent.OnCommitDrafts) },
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Commit")
        }
        OutlinedButton(
            onClick = { viewModel.processIntent(GraphIntent.OnDiscardDrafts) },
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Discard")
        }
    }
}

/**
 * The canvas plus everything floating over it: draft strip and selected-node bar
 * (stacked at the top so they never overlap), zoom/fit controls, add button, dialogs
 * and error snackbars. Shared by both layouts.
 */
@Composable
private fun GraphCanvasArea(
    viewModel: GraphViewModel,
    state: GraphState,
    canvasState: CanvasState,
    showDraftStrip: Boolean,
    controlsBottomPadding: Dp
) {
    var pendingLink by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showLinksFor by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val selected = state.selectedNodeId?.let { state.nodes[it] }

    // Surface errors (failed AI requests, sync problems) that would otherwise go unseen.
    val message = state.error ?: state.syncStatus
    LaunchedEffect(message) {
        if (message == null) return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = if (state.error != null && state.queuedRequests.isNotEmpty()) "Retry" else null,
            withDismissAction = true,
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) viewModel.processIntent(GraphIntent.RetryQueuedRequests)
        viewModel.processIntent(GraphIntent.OnErrorShown)
    }

    Box(Modifier.fillMaxSize()) {
        OmniMapCanvas(
            viewModel = viewModel,
            canvasState = canvasState,
            onLinkDropped = { source, target -> pendingLink = source to target },
            onLinkRequest = { showLinksFor = state.selectedNodeId }
        )

        // Top: draft strip (when the panel isn't showing it) above the selected-node bar.
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp, start = 16.dp, end = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showDraftStrip && state.draftCount > 0) {
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.tertiaryContainer, tonalElevation = 2.dp) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(changesLabel(state.draftCount), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        DraftButtons(viewModel)
                    }
                }
            }

            if (selected != null) {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 2.dp) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NodeTypeGlyph(selected.type)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = selected.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 280.dp).padding(end = 8.dp)
                        )
                        IconButton(onClick = { showLinksFor = selected.id }) {
                            Icon(Icons.Filled.Link, contentDescription = "Links", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.processIntent(GraphIntent.OnEditNodeRequest(selected.id)) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Node", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.processIntent(GraphIntent.OnNodeDeleted(selected.id)) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Node", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Bottom-right: zoom and fit controls above the add button.
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = controlsBottomPadding),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 2.dp) {
                Column {
                    IconButton(onClick = { canvasState.zoomBy(1.2f) }) { Icon(Icons.Filled.Add, contentDescription = "Zoom in") }
                    IconButton(onClick = { canvasState.zoomBy(1 / 1.2f) }) { Icon(Icons.Filled.Remove, contentDescription = "Zoom out") }
                    IconButton(onClick = { canvasState.fitAll(state.nodes.values) }) { Icon(Icons.Filled.FitScreen, contentDescription = "Fit all nodes (F)") }
                }
            }
            ExtendedFloatingActionButton(
                onClick = { viewModel.processIntent(GraphIntent.OnCreateNodeRequest(true)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New node") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = controlsBottomPadding))
    }

    state.editingNode?.let { node ->
        EditNodeDialog(
            node = node,
            onDismiss = { viewModel.processIntent(GraphIntent.OnEditNodeRequest(null)) },
            onSave = { title, desc ->
                viewModel.processIntent(GraphIntent.OnNodeUpdated(node.id, title, desc))
            }
        )
    }

    if (state.isCreatingNode) {
        CreateNodeDialog(
            onDismiss = { viewModel.processIntent(GraphIntent.OnCreateNodeRequest(false)) },
            onSave = { title, desc, type ->
                // Next to the selected node if there is one, otherwise in the middle of the view.
                val position = selected?.let {
                    val bounds = canvasState.boundsOf(it)
                    Offset(bounds.right + 64f, bounds.top)
                } ?: (canvasState.visibleCenter() - Offset(CanvasState.DefaultNodeSize.width.value / 2, CanvasState.DefaultNodeSize.height.value / 2))
                viewModel.processIntent(GraphIntent.OnNodeCreated(type, title, desc, position.x, position.y))
                viewModel.processIntent(GraphIntent.OnCreateNodeRequest(false))
            }
        )
    }

    pendingLink?.let { (sourceId, targetId) ->
        val source = state.nodes[sourceId]
        val target = state.nodes[targetId]
        if (source == null || target == null) {
            pendingLink = null
        } else {
            LinkTypeDialog(
                source = source,
                target = target,
                onDismiss = { pendingLink = null },
                onConfirm = { type ->
                    viewModel.processIntent(GraphIntent.OnEdgeCreated(sourceId, targetId, type))
                    pendingLink = null
                }
            )
        }
    }

    showLinksFor?.let { id ->
        val node = state.nodes[id]
        if (node == null) {
            showLinksFor = null
        } else {
            LinksDialog(
                node = node,
                nodes = state.nodes,
                edges = state.edges,
                onDismiss = { showLinksFor = null },
                onCreate = { targetId, type -> viewModel.processIntent(GraphIntent.OnEdgeCreated(id, targetId, type)) },
                onChangeType = { edgeId, type -> viewModel.processIntent(GraphIntent.OnEdgeTypeChanged(edgeId, type)) },
                onDelete = { edgeId -> viewModel.processIntent(GraphIntent.OnEdgeDeleted(edgeId)) }
            )
        }
    }
}
