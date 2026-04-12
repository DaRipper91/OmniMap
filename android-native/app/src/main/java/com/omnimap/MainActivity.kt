package com.omnimap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.omnimap.presentation.OmniMapApp
import com.omnimap.presentation.dashboard.DashboardViewModel
import com.omnimap.presentation.graph.GraphViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Task 1: Immersive Edge-to-Edge display for bezelless UX
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val appContainer = (application as OmniMapApplication).container
            
            // Manual Dependency Injection Factory
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(GraphViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return GraphViewModel(
                            repository = appContainer.omniMapRepository,
                            hapticEngine = appContainer.hapticEngine,
                            aiRepository = appContainer.aiInferenceRepository
                        ) as T
                    }
                    if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return DashboardViewModel(repository = appContainer.omniMapRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }

            val graphViewModel: GraphViewModel = viewModel(factory = factory)
            val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)

            OmniMapApp(
                graphViewModel = graphViewModel,
                dashboardViewModel = dashboardViewModel
            )
        }
    }
}