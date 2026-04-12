package com.omnimap

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.omnimap.core.haptics.DesktopHapticEngine
import com.omnimap.data.repository.DesktopAiInferenceRepositoryImpl
import com.omnimap.data.repository.DesktopOmniMapRepositoryImpl
import com.omnimap.presentation.OmniMapApp
import com.omnimap.presentation.dashboard.DashboardViewModel
import com.omnimap.presentation.graph.GraphViewModel

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "OmniMap Desktop") {
        
        // Manual DI for the Desktop Target
        val repository = DesktopOmniMapRepositoryImpl()
        val hapticEngine = DesktopHapticEngine()
        val aiRepository = DesktopAiInferenceRepositoryImpl()

        val graphViewModel = GraphViewModel(
            repository = repository,
            hapticEngine = hapticEngine,
            aiRepository = aiRepository
        )
        val dashboardViewModel = DashboardViewModel(
            repository = repository
        )

        OmniMapApp(
            graphViewModel = graphViewModel,
            dashboardViewModel = dashboardViewModel
        )
    }
}