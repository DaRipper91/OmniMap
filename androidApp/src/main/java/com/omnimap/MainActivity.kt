package com.omnimap

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.omnimap.data.repository.AndroidAuthRepository
import com.omnimap.presentation.OmniMapApp
import com.omnimap.presentation.dashboard.DashboardViewModel
import com.omnimap.presentation.graph.GraphViewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val container = (application as OmniMapApplication).container
        
        handleIntent(intent, container.authRepository)

        setContent {
            val scope = rememberCoroutineScope()
            var jsonToExport by remember { mutableStateOf<String?>(null) }
            
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

            val exportLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                if (uri != null && jsonToExport != null) {
                    contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(jsonToExport!!.toByteArray())
                    }
                }
                jsonToExport = null
            }

            val importLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    contentResolver.openInputStream(uri)?.use { stream ->
                        val jsonText = stream.bufferedReader().use { it.readText() }
                        dashboardViewModel.importGraph(jsonText)
                    }
                }
            }

            OmniMapApp(
                graphViewModel = graphViewModel,
                dashboardViewModel = dashboardViewModel,
                authRepository = container.authRepository,
                onImportRequest = { importLauncher.launch(arrayOf("application/json")) },
                onExportRequest = { json ->
                    jsonToExport = json
                    exportLauncher.launch("omnimap_backup.json")
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val container = (application as OmniMapApplication).container
        handleIntent(intent, container.authRepository)
    }

    private fun handleIntent(intent: Intent?, authRepository: com.omnimap.domain.repository.AuthRepository) {
        intent?.data?.let { uri ->
            if (uri.scheme == "com.omnimap" && uri.host == "auth") {
                (authRepository as? AndroidAuthRepository)?.handleRedirect(uri)
            }
        }
    }
}
