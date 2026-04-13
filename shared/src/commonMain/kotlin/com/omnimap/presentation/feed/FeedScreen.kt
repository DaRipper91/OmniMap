package com.omnimap.presentation.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.omnimap.presentation.graph.GraphIntent
import com.omnimap.presentation.graph.GraphViewModel
import com.omnimap.presentation.graph.chat.ChatContent

@Composable
fun FeedScreen(viewModel: GraphViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141218)) // MD3 Background Dark
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Text(
            text = "Live Intelligence Feed",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ChatContent(
            state = state,
            onPromptSubmitted = { prompt -> 
                viewModel.processIntent(GraphIntent.OnSubmitPrompt(prompt)) 
            },
            modifier = Modifier.weight(1f)
        )
    }
}
