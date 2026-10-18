package com.omnimap.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.omnimap.presentation.dashboard.DashboardScreen
import com.omnimap.presentation.dashboard.DashboardViewModel
import com.omnimap.presentation.feed.FeedScreen
import com.omnimap.presentation.graph.GraphScreen
import com.omnimap.presentation.graph.GraphViewModel
import com.omnimap.presentation.navigation.Screen
import com.omnimap.presentation.theme.OmniMapTheme
import com.omnimap.presentation.welcome.WelcomeScreen

/** Windows at least this wide (laptops, desktops, tablets in landscape) get the wide layout. */
private val WideLayoutMinWidth = 840.dp

private data class NavItem(val screen: Screen, val label: String, val icon: ImageVector, val description: String)

private val navItems = listOf(
    NavItem(Screen.Dashboard, "Search", Icons.Filled.Dashboard, "Dashboard"),
    NavItem(Screen.Graph, "Graph", Icons.Filled.Hub, "Mind Map"),
    NavItem(Screen.Feed, "AI Feed", Icons.Filled.DynamicFeed, "Feed")
)

@Composable
fun OmniMapApp(
    graphViewModel: GraphViewModel,
    dashboardViewModel: DashboardViewModel,
    authRepository: com.omnimap.domain.repository.AuthRepository,
    /** Installed agent CLIs (`cli:claude`, …); desktop only. */
    agentClis: List<String> = emptyList(),
    onImportRequest: () -> Unit = {},
    onExportRequest: (String) -> Unit = {}
) {
    OmniMapTheme {
        var currentScreen by remember {
            mutableStateOf<Screen>(if (graphViewModel.isAiConfigured()) Screen.Dashboard else Screen.Welcome)
        }

        val content: @Composable (Boolean) -> Unit = { isWide ->
            when (currentScreen) {
                Screen.Welcome -> {
                    WelcomeScreen(
                        authRepository = authRepository,
                        agentClis = agentClis,
                        onConfigurationFinished = { key, model, url ->
                            graphViewModel.saveAiConfiguration(key, model, url)
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
                        },
                        onImportRequest = onImportRequest,
                        onExportRequest = onExportRequest
                    )
                }
                Screen.Graph -> {
                    GraphScreen(viewModel = graphViewModel, isWide = isWide)
                }
                Screen.Feed -> {
                    FeedScreen(viewModel = graphViewModel)
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= WideLayoutMinWidth
            val showNav = currentScreen != Screen.Welcome

            if (isWide) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (showNav) {
                            NavigationRail(
                                modifier = Modifier.fillMaxHeight(),
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                navItems.forEach { item ->
                                    NavigationRailItem(
                                        selected = currentScreen == item.screen,
                                        onClick = { currentScreen = item.screen },
                                        icon = { Icon(item.icon, contentDescription = item.description) },
                                        label = { Text(item.label) },
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        content(true)
                    }
                }
            } else {
                Scaffold(
                    bottomBar = {
                        if (showNav) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                navItems.forEach { item ->
                                    NavigationBarItem(
                                        selected = currentScreen == item.screen,
                                        onClick = { currentScreen = item.screen },
                                        icon = { Icon(item.icon, contentDescription = item.description) },
                                        label = { Text(item.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        content(false)
                    }
                }
            }
        }
    }
}
