package com.omnimap

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.omnimap.di.DesktopAppContainer
import com.omnimap.presentation.OmniMapApp
import com.omnimap.presentation.dashboard.DashboardViewModel
import com.omnimap.presentation.graph.GraphViewModel

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

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
            dashboardViewModel = dashboardViewModel,
            authRepository = container.authRepository,
            onImportRequest = {
                val chooser = JFileChooser().apply {
                    fileFilter = FileNameExtensionFilter("JSON Files", "json")
                }
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    val file = chooser.selectedFile
                    dashboardViewModel.importGraph(file.readText())
                }
            },
            onExportRequest = { json ->
                val chooser = JFileChooser().apply {
                    fileFilter = FileNameExtensionFilter("JSON Files", "json")
                    selectedFile = File("omnimap_backup.json")
                }
                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    val file = chooser.selectedFile
                    file.writeText(json)
                }
            }
        )
    }
}
