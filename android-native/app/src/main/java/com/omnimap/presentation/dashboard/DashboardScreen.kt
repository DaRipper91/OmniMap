package com.omnimap.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNodeClick: (String) -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141218)) // MD3 Background Dark
            .padding(16.dp)
    ) {
        Text(
            text = "OmniMap Dashboard",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp, top = 32.dp)
        )

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
                containerColor = Color(0xFF1C1B1F) // MD3 Surface
            )
        ) { }

        Spacer(modifier = Modifier.height(16.dp))

        if (results.isNotEmpty()) {
            Text(
                text = "Search Results",
                color = Color(0xFFD0BCFF),
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = node.title, color = Color.White, fontWeight = FontWeight.Bold)
                            if (node.description.isNotBlank()) {
                                Text(text = node.description, color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        } else if (query.isNotBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No nodes found matching your query.", color = Color.Gray)
            }
        } else {
            // Dashboard placeholder content
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Start typing to search your graph globally.", color = Color.DarkGray)
            }
        }
    }
}