package com.omnimap.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.omnimap.presentation.dashboard.DashboardScreen
import com.omnimap.presentation.dashboard.DashboardViewModel
import com.omnimap.presentation.graph.GraphIntent
import com.omnimap.presentation.graph.GraphScreen
import com.omnimap.presentation.graph.GraphViewModel
import com.omnimap.presentation.navigation.Screen

@Composable
fun OmniMapApp(graphViewModel: GraphViewModel, dashboardViewModel: DashboardViewModel) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Color(0xFF141218), // MD3 Background Dark
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            NavigationBar(
                containerColor = Color(0xFF1C1B1F), // MD3 Dark Surface
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Dashboard.route } == true,
                    onClick = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF49454F)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Share, contentDescription = "Graph") },
                    label = { Text("Graph") },
                    selected = currentDestination?.hierarchy?.any { it.route == Screen.Graph.route } == true,
                    onClick = {
                        navController.navigate(Screen.Graph.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF49454F)
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Graph.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { 
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNodeClick = { nodeId ->
                        // Navigate to graph and select the node
                        graphViewModel.processIntent(GraphIntent.OnNodeSelected(nodeId))
                        navController.navigate(Screen.Graph.route)
                    }
                ) 
            }
            composable(Screen.Graph.route) { GraphScreen(viewModel = graphViewModel) }
        }
    }
}