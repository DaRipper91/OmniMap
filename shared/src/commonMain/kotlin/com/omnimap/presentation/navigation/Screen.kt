package com.omnimap.presentation.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Graph : Screen("graph")
    data object Feed : Screen("feed")
    data object Welcome : Screen("welcome")
}