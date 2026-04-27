package com.omnimap.presentation.graph.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.omnimap.presentation.graph.GraphIntent
import com.omnimap.presentation.graph.GraphState
import com.omnimap.presentation.graph.GraphViewModel

@Composable
fun IntelligentBottomSheet(viewModel: GraphViewModel, state: GraphState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = 500.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        ChatContent(
            state = state,
            onPromptSubmitted = { prompt -> 
                viewModel.processIntent(GraphIntent.OnSubmitPrompt(prompt)) 
            },
            onRetryClicked = {
                viewModel.processIntent(GraphIntent.RetryQueuedRequests)
            }
        )
    }
}
