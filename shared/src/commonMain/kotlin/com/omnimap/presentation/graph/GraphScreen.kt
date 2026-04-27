package com.omnimap.presentation.graph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.omnimap.presentation.graph.chat.IntelligentBottomSheet
import com.omnimap.presentation.graph.ui.CreateNodeDialog
import com.omnimap.presentation.graph.ui.EditNodeDialog
import com.omnimap.presentation.graph.ui.OmniMapCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(viewModel: GraphViewModel) {
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
            sheetContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            content = { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    // The main canvas
                    OmniMapCanvas(viewModel = viewModel)
                }
            }
        )

        // FAB for creating nodes
        FloatingActionButton(
            onClick = { viewModel.processIntent(GraphIntent.OnCreateNodeRequest(true)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp), // Padding to avoid overlap with sheet
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Node")
        }

        // Suggestion 3 Flavor: The "Properties Panel" Row
        if (state.selectedNodeId != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.nodes[state.selectedNodeId]?.title ?: "",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(end = 16.dp)
                )
                IconButton(onClick = { viewModel.processIntent(GraphIntent.OnEditNodeRequest(state.selectedNodeId)) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Node", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { viewModel.processIntent(GraphIntent.OnNodeDeleted(state.selectedNodeId!!)) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Node", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Suggestion 1 & 2 Flavor: The Full-Screen / Large Floating Editor
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
                    viewModel.processIntent(GraphIntent.OnNodeCreated(type, title, desc, 250f, 250f))
                    viewModel.processIntent(GraphIntent.OnCreateNodeRequest(false))
                }
            )
        }
    }
}
