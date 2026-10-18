package com.omnimap

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.omnimap.data.repository.CliAiRepository
import com.omnimap.di.DesktopAppContainer
import com.omnimap.presentation.OmniMapApp
import com.omnimap.presentation.dashboard.DashboardViewModel
import com.omnimap.presentation.graph.GraphViewModel

import java.io.File
import javax.imageio.ImageIO
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

    val appIcon = remember {
        object {}.javaClass.getResourceAsStream("/omnimap.png")!!.use { stream ->
            BitmapPainter(ImageIO.read(stream).toComposeImageBitmap())
        }
    }

    Window(
        onCloseRequest = {
            container.shutdown()
            exitApplication()
        },
        title = "OmniMap",
        icon = appIcon,
        state = rememberWindowState(size = DpSize(1280.dp, 820.dp))
    ) {
        OmniMapApp(
            graphViewModel = graphViewModel,
            dashboardViewModel = dashboardViewModel,
            authRepository = container.authRepository,
            agentClis = remember { CliAiRepository.installed() },
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
