package com.omnimap

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.omnimap.di.DesktopAppContainer
import com.omnimap.presentation.OmniMapApp
import com.omnimap.presentation.dashboard.DashboardViewModel
import com.omnimap.presentation.graph.GraphViewModel

fun main() = application {
    val container = remember { DesktopAppContainer() }
    val graphViewModel = remember { 
        GraphViewModel(
            repository = container.omniMapRepository,
            hapticEngine = container.hapticEngine,
            aiRepository = container.aiInferenceRepository,
            connectivityObserver = container.connectivityObserver
        )
    }
    val dashboardViewModel = remember {
        DashboardViewModel(repository = container.omniMapRepository)
    }

    Window(onCloseRequest = ::exitApplication, title = "OmniMap") {
        OmniMapApp(
            graphViewModel = graphViewModel,
            dashboardViewModel = dashboardViewModel
        )
    }
}
