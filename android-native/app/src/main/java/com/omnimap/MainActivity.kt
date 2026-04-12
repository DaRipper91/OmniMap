package com.omnimap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.omnimap.presentation.OmniMapApp
import com.omnimap.presentation.graph.GraphViewModel

class MainActivity : ComponentActivity() {
    // Note: In a production setup this would be managed by Hilt or Koin,
    // but we use a lateinit placeholder for the foundation phase.
    private lateinit var graphViewModel: GraphViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Task 1: Immersive Edge-to-Edge display for bezelless UX
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Note: initialize the view model graphViewModel here via DI or factory.

        setContent {
            OmniMapApp(graphViewModel = graphViewModel)
        }
    }
}