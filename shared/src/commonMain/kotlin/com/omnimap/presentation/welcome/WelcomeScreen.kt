package com.omnimap.presentation.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onApiKeyEntered: (String) -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var apiKey by remember { mutableStateOf("") }

    val steps = listOf(
        OnboardingStep(
            title = "Your Mind, Visualized",
            description = "OmniMap is a high-performance execution control plane for your thoughts. Capture ideas, tasks, and goals in a native, tactile graph.",
            icon = Icons.Filled.AutoAwesome
        ),
        OnboardingStep(
            title = "AI-Powered Flow",
            description = "The OmniMap Architect (Gemini) doesn't just chat—it builds. It proposes real-time mutations to your graph as you dialogue with it.",
            icon = Icons.Filled.Hub
        ),
        OnboardingStep(
            title = "Tactile Precision",
            description = "Optimized for 120Hz native performance. Drag, connect, and feel your mind-map snap into place with physical haptic feedback.",
            icon = Icons.Filled.Vibration
        ),
        OnboardingStep(
            title = "Intelligence Setup",
            description = "To enable AI features, enter your Gemini API key. It's stored locally and never leaves your device.",
            icon = Icons.Filled.Key,
            isLast = true
        )
    )

    val currentStep = steps[step]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141218))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = currentStep.icon,
            contentDescription = null,
            tint = Color(0xFFD0BCFF),
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = currentStep.title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = currentStep.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        if (currentStep.isLast) {
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("Gemini API Key") },
                placeholder = { Text("Paste your API key here...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFFD0BCFF),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFD0BCFF),
                    cursorColor = Color(0xFFD0BCFF)
                )
            )
            Text(
                text = "Get one for free at ai.google.dev",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp).clickable { /* Open Browser TODO */ }
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Progress indicators
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            steps.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == step) 12.dp else 8.dp)
                        .background(
                            if (index == step) Color(0xFFD0BCFF) else Color.Gray,
                            RoundedCornerShape(50)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                if (currentStep.isLast) {
                    if (apiKey.isNotBlank()) onApiKeyEntered(apiKey)
                } else {
                    step++
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD0BCFF),
                contentColor = Color(0xFF381E72)
            ),
            enabled = !currentStep.isLast || apiKey.isNotBlank()
        ) {
            Text(
                if (currentStep.isLast) "Initialize Mind-Map" else "Next",
                fontWeight = FontWeight.Bold
            )
        }

        if (step > 0) {
            TextButton(
                onClick = { step-- },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Back", color = Color.Gray)
            }
        }
    }
}

private data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isLast: Boolean = false
)
