package com.omnimap.presentation

import androidx.compose.runtime.*
import com.omnimap.presentation.dashboard.DashboardScreen
import com.omnimap.presentation.dashboard.DashboardViewModel
import com.omnimap.presentation.feed.FeedScreen
import com.omnimap.presentation.graph.GraphScreen
import com.omnimap.presentation.graph.GraphViewModel
import com.omnimap.presentation.navigation.Screen

@Composable
fun OmniMapApp(
    graphViewModel: GraphViewModel,
    dashboardViewModel: DashboardViewModel
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    // Simple navigation shell
    when (currentScreen) {
        Screen.Dashboard -> {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNodeClick = {
                    currentScreen = Screen.Graph
                }
            )
        }
        Screen.Graph -> {
            GraphScreen(viewModel = graphViewModel)
        }
        Screen.Feed -> {
            FeedScreen()
        }
    }
}
