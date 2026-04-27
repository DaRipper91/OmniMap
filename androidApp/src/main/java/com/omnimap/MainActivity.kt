package com.omnimap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.omnimap.presentation.OmniMapApp
import com.omnimap.presentation.dashboard.DashboardViewModel
import com.omnimap.presentation.graph.GraphViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val container = (application as OmniMapApplication).container

        setContent {
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

            OmniMapApp(
                graphViewModel = graphViewModel,
                dashboardViewModel = dashboardViewModel
            )
        }
    }
}
