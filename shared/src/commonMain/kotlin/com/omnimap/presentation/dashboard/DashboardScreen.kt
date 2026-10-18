package com.omnimap.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.omnimap.domain.model.Node
import com.omnimap.domain.model.NodeType
import com.omnimap.presentation.graph.GraphState
import com.omnimap.presentation.theme.NodeTypeGlyph
import com.omnimap.presentation.theme.accent
import com.omnimap.presentation.theme.label

/**
 * Home: global search, plus an overview of the graph (counts per type, recent nodes)
 * when nothing is typed. Opening a node shows it centred on the canvas.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    graphState: GraphState,
    onNodeClick: (String) -> Unit,
    onImportRequest: () -> Unit,
    onExportRequest: (String) -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val exportJson by viewModel.exportJsonResult.collectAsState()

    LaunchedEffect(exportJson) {
        if (exportJson != null) {
            onExportRequest(exportJson!!)
            viewModel.clearExportResult()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OmniMap",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(onClick = onImportRequest) {
                        Icon(Icons.Filled.Upload, contentDescription = "Import JSON", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { viewModel.exportGraph() }) {
                        Icon(Icons.Filled.Download, contentDescription = "Export JSON", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            SearchBar(
                query = query,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onSearch = { },
                active = false,
                onActiveChange = { },
                placeholder = { Text("Search thoughts, tasks, and nodes...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth(),
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) { }

            Spacer(modifier = Modifier.height(24.dp))

            when {
                query.isNotBlank() && results.isEmpty() -> Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                    Text("No nodes match “$query”.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                }
                query.isNotBlank() -> {
                    SectionLabel("Search results")
                    NodeList(results, onNodeClick)
                }
                graphState.nodes.isEmpty() -> Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Your map is empty. Open Graph to add your first node, or ask the Architect.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                else -> {
                    val nodes = graphState.nodes.values
                    val drafts = nodes.count { it.isDraft } + graphState.edges.count { it.isDraft }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatTile(nodes.size.toString(), if (nodes.size == 1) "node" else "nodes")
                        StatTile(graphState.edges.size.toString(), if (graphState.edges.size == 1) "link" else "links")
                        if (drafts > 0) StatTile(drafts.toString(), if (drafts == 1) "draft to review" else "drafts to review")
                    }
                    Spacer(Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        NodeType.entries.forEach { type ->
                            val count = nodes.count { it.type == type }
                            if (count > 0) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("$count ${type.label}") },
                                    leadingIcon = { NodeTypeGlyph(type) }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    SectionLabel("Recently changed")
                    NodeList(nodes.sortedByDescending { it.updatedAt }.take(8), onNodeClick)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun StatTile(value: String, label: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun NodeList(nodes: List<Node>, onNodeClick: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(nodes, key = { it.id }) { node ->
            Card(
                onClick = { onNodeClick(node.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    NodeTypeGlyph(node.type, Modifier.padding(top = 6.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = node.title,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Text(
                                text = "  ${node.type.label}" + if (node.isDraft) " · AI draft" else "",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (node.isDraft) MaterialTheme.colorScheme.tertiary else node.type.accent
                            )
                        }
                        if (node.description.isNotBlank()) {
                            Text(
                                text = node.description,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
