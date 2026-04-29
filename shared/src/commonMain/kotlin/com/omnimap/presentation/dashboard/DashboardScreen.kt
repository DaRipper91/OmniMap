package com.omnimap.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 32.dp),
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

        // Suggestion 1: Global Search Bar
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
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) { }

        Spacer(modifier = Modifier.height(16.dp))

        if (results.isNotEmpty()) {
            Text(
                text = "Search Results",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { node ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNodeClick(node.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = node.title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            if (node.description.isNotBlank()) {
                                Text(
                                    text = node.description, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), 
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        } else if (query.isNotBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No nodes found matching your query.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        } else {
            // Dashboard placeholder content
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Start typing to search your graph globally.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
            }
        }
    }
}
