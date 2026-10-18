package com.omnimap.presentation.graph.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omnimap.presentation.graph.GraphState

@Composable
fun ChatContent(
    state: GraphState,
    onPromptSubmitted: (String) -> Unit,
    onRetryClicked: () -> Unit,
    /** The unsent message; owned by the caller so it outlives this composable. */
    promptText: String,
    onPromptTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val submit = {
        if (promptText.isNotBlank()) {
            onPromptSubmitted(promptText)
            onPromptTextChange("")
        }
    }
    
    val selectedTitle = state.selectedNodeId?.let { state.nodes[it]?.title }
    val contextTitle = if (selectedTitle != null) "Talking about: $selectedTitle" else "Global AI Architect"
    val starters = if (selectedTitle != null) {
        listOf("Break “$selectedTitle” into tasks", "What could block “$selectedTitle”?", "Suggest ideas related to “$selectedTitle”")
    } else {
        listOf("Break my goal into tasks", "What am I missing?", "Suggest links between my ideas", "Summarise my graph")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = contextTitle,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (state.queuedRequests.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (state.queuedRequests.size == 1) "1 request waiting to retry." else "${state.queuedRequests.size} requests waiting to retry.",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onRetryClicked) {
                    Text("Retry", color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }

        // Chat History
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true
        ) {
            if (state.isAiThinking) {
                item {
                    Text("Architect is thinking...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.padding(8.dp))
                }
            }

            items(state.chatHistory.reversed(), key = { it.id }) { msg ->
                val align = if (msg.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
                val bg = when {
                    msg.isFromUser -> MaterialTheme.colorScheme.primaryContainer
                    msg.isError -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                }
                val textColor = when {
                    msg.isFromUser -> MaterialTheme.colorScheme.onPrimaryContainer
                    msg.isError -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
                
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = align) {
                    Column(
                        modifier = Modifier
                            .padding(4.dp)
                            .widthIn(max = 320.dp)
                            .background(bg, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = msg.text, color = textColor)
                        if (msg.isError && state.queuedRequests.isNotEmpty()) {
                            TextButton(onClick = onRetryClicked, contentPadding = PaddingValues(horizontal = 0.dp)) {
                                Text("Retry", color = textColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Empty chat: say what the Architect does and offer starting points.
            if (state.chatHistory.isEmpty() && !state.isAiThinking) {
                item {
                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Ask the Architect to grow your map. It proposes nodes and links as drafts you commit or discard.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                        starters.forEach { starter ->
                            SuggestionChip(onClick = { onPromptSubmitted(starter) }, label = { Text(starter) })
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = promptText,
                onValueChange = onPromptTextChange,
                // Hardware keyboards: Enter sends, Shift+Enter starts a new line.
                modifier = Modifier
                    .weight(1f)
                    .onPreviewKeyEvent { event ->
                        if (event.key == Key.Enter && !event.isShiftPressed) {
                            if (event.type == KeyEventType.KeyDown) submit()
                            true
                        } else {
                            false
                        }
                    },
                placeholder = { Text("Instruct the architect...") },
                // A lifted fill with no underline, so the field reads as a field on the panel.
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
            IconButton(
                onClick = submit,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
