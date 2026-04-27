package com.omnimap.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.omnimap.presentation.dashboard.DashboardScreen
import com.omnimap.presentation.dashboard.DashboardViewModel
import com.omnimap.presentation.feed.FeedScreen
import com.omnimap.presentation.graph.GraphScreen
import com.omnimap.presentation.graph.GraphViewModel
import com.omnimap.presentation.navigation.Screen
import com.omnimap.presentation.welcome.WelcomeScreen

@Composable
fun OmniMapApp(
    graphViewModel: GraphViewModel,
    dashboardViewModel: DashboardViewModel
) {
    var currentScreen by remember { 
        mutableStateOf<Screen>(if (graphViewModel.isAiConfigured()) Screen.Dashboard else Screen.Welcome) 
    }

    Scaffold(
        bottomBar = {
            if (currentScreen != Screen.Welcome) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.Dashboard,
                        onClick = { currentScreen = Screen.Dashboard },
                        icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Search") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Graph,
                        onClick = { currentScreen = Screen.Graph },
                        icon = { Icon(Icons.Filled.Hub, contentDescription = "Mind Map") },
                        label = { Text("Graph") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Feed,
                        onClick = { currentScreen = Screen.Feed },
                        icon = { Icon(Icons.Filled.DynamicFeed, contentDescription = "Feed") },
                        label = { Text("AI Feed") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.Welcome -> {
                    WelcomeScreen(
                        onApiKeyEntered = { key ->
                            graphViewModel.saveApiKey(key)
                            currentScreen = Screen.Dashboard
                        }
                    )
                }
                Screen.Dashboard -> {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNodeClick = { nodeId ->
                            // TODO: Focus specific node in graph
                            currentScreen = Screen.Graph
                        }
                    )
                }
                Screen.Graph -> {
                    GraphScreen(viewModel = graphViewModel)
                }
                Screen.Feed -> {
                    FeedScreen(viewModel = graphViewModel)
                }
            }
        }
    }
}
