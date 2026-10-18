package com.omnimap.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.omnimap.presentation.dashboard.DashboardScreen
import com.omnimap.presentation.dashboard.DashboardViewModel
import com.omnimap.presentation.feed.FeedScreen
import com.omnimap.presentation.graph.GraphScreen
import com.omnimap.presentation.graph.GraphIntent
import com.omnimap.presentation.graph.GraphViewModel
import com.omnimap.presentation.graph.ui.CanvasState
import com.omnimap.presentation.settings.SettingsScreen
import com.omnimap.presentation.navigation.Screen
import com.omnimap.presentation.theme.OmniMapTheme
import com.omnimap.presentation.welcome.WelcomeScreen

/** Windows at least this wide (laptops, desktops, tablets in landscape) get the wide layout. */
private val WideLayoutMinWidth = 840.dp

private data class NavItem(val screen: Screen, val label: String, val icon: ImageVector, val description: String)

private val navItems = listOf(
    NavItem(Screen.Dashboard, "Search", Icons.Filled.Dashboard, "Dashboard"),
    NavItem(Screen.Graph, "Graph", Icons.Filled.Hub, "Mind Map"),
    NavItem(Screen.Feed, "AI Feed", Icons.Filled.DynamicFeed, "Feed"),
    NavItem(Screen.Settings, "Settings", Icons.Filled.Settings, "Settings")
)

/** On wide windows the Architect chat already sits beside the graph, so AI Feed is phone-only. */
private fun navItemsFor(isWide: Boolean) = if (isWide) navItems.filter { it.screen != Screen.Feed } else navItems

@Composable
fun OmniMapApp(
    graphViewModel: GraphViewModel,
    dashboardViewModel: DashboardViewModel,
    authRepository: com.omnimap.domain.repository.AuthRepository,
    /** Installed agent CLIs (`cli:claude`, …); desktop only. */
    agentClis: List<String> = emptyList(),
    /** Seeds the Settings screen with the saved provider. */
    settingsManager: com.omnimap.core.settings.SettingsManager? = null,
    onImportRequest: () -> Unit = {},
    onExportRequest: (String) -> Unit = {}
) {
    OmniMapTheme {
        var currentScreen by remember {
            mutableStateOf<Screen>(if (graphViewModel.isAiConfigured()) Screen.Dashboard else Screen.Welcome)
        }

        // Hoisted so zoom and pan survive switching tabs.
        val canvasState = remember { CanvasState() }
        val graphState by graphViewModel.state.collectAsState()

        val content: @Composable (Boolean) -> Unit = { isWide ->
            // AI Feed isn't offered on wide windows; show the graph (with its chat panel) instead.
            val screen = if (isWide && currentScreen == Screen.Feed) Screen.Graph else currentScreen
            when (screen) {
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
                        graphState = graphState,
                        onNodeClick = { nodeId ->
                            graphViewModel.processIntent(GraphIntent.OnFocusNode(nodeId))
                            currentScreen = Screen.Graph
                        },
                        onImportRequest = onImportRequest,
                        onExportRequest = onExportRequest
                    )
                }
                Screen.Graph -> {
                    GraphScreen(viewModel = graphViewModel, canvasState = canvasState, isWide = isWide)
                }
                Screen.Feed -> {
                    FeedScreen(viewModel = graphViewModel)
                }
                Screen.Settings -> {
                    SettingsScreen(
                        settingsManager = settingsManager,
                        agentClis = agentClis,
                        onSave = { choice -> graphViewModel.saveAiConfiguration(choice.apiKey, choice.model, choice.baseUrl) }
                    )
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= WideLayoutMinWidth
            val showNav = currentScreen != Screen.Welcome

            if (!showNav) {
                // Setup is composed in the same place at any width, so resizing across the
                // breakpoint doesn't reset the wizard or clear what was typed.
                Surface(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                    content(isWide)
                }
            } else if (isWide) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Android draws edge to edge; keep the rail and panel clear of system bars.
                    Row(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                        if (showNav) {
                            NavigationRail(
                                modifier = Modifier.fillMaxHeight(),
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                navItemsFor(isWide = true).forEach { item ->
                                    NavigationRailItem(
                                        selected = currentScreen == item.screen || (currentScreen == Screen.Feed && item.screen == Screen.Graph),
                                        onClick = { currentScreen = item.screen },
                                        icon = { Icon(item.icon, contentDescription = item.description) },
                                        label = { Text(item.label) },
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        content(true)
                    }
                }
            } else {
                Scaffold(
                    bottomBar = {
                        if (showNav) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                navItemsFor(isWide = false).forEach { item ->
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
