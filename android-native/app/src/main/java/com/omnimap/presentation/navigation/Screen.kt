package com.omnimap.presentation.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Graph : Screen("graph")
    object Feed : Screen("feed")
}