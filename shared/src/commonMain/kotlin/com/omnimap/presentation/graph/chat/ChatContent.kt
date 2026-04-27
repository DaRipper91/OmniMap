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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omnimap.presentation.graph.GraphState

@Composable
fun ChatContent(
    state: GraphState,
    onPromptSubmitted: (String) -> Unit,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var promptText by remember { mutableStateOf("") }
    
    val contextTitle = if (state.selectedNodeId != null) {
        val nodeTitle = state.nodes[state.selectedNodeId]?.title ?: "Unknown"
        "Talking about: $nodeTitle"
    } else {
        "Global AI Architect"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = contextTitle,
            color = Color(0xFFD0BCFF), // Primary
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
                    .background(Color(0x33FFFF00), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${state.queuedRequests.size} requests queued.",
                    color = Color.Yellow,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onRetryClicked) {
                    Text("Retry", color = Color.Yellow)
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
                    Text("Architect is thinking...", color = Color.Gray, modifier = Modifier.padding(8.dp))
                }
            }

            items(state.chatHistory.reversed()) { msg ->
                val align = if (msg.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
                val bg = if (msg.isFromUser) Color(0xFF4A4458) else Color(0xFF1D1B20)
                
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = align) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(bg, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = msg.text, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Field
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = promptText,
                onValueChange = { promptText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Instruct the architect...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1D1B20),
                    unfocusedContainerColor = Color(0xFF1D1B20),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            )
            IconButton(
                onClick = {
                    if (promptText.isNotBlank()) {
                        onPromptSubmitted(promptText)
                        promptText = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color(0xFFD0BCFF))
            }
        }
    }
}
