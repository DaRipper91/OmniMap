package com.omnimap.presentation.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
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
    onConfigurationFinished: (String, String, String?) -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var apiKey by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("gemini-1.5-pro") }
    var baseUrl by remember { mutableStateOf("https://api.openai.com/v1/") }
    var expanded by remember { mutableStateOf(false) }

    val models = listOf(
        "gemini-1.5-pro",
        "gemini-1.5-flash",
        "llama3",
        "llama3.1",
        "qwen2.5"
    )

    val steps = listOf(
        OnboardingStep(
            title = "Your Mind, Visualized",
            description = "OmniMap is a high-performance execution control plane for your thoughts. Capture ideas, tasks, and goals in a native, tactile graph.",
            icon = Icons.Filled.AutoAwesome
        ),
        OnboardingStep(
            title = "AI-Powered Flow",
            description = "The OmniMap Architect doesn't just chat—it builds. It proposes real-time mutations to your graph as you dialogue with it.",
            icon = Icons.Filled.Hub
        ),
        OnboardingStep(
            title = "Model Selection",
            description = "Choose the brain for your OmniMap. We support Gemini, Llama, and Qwen.",
            icon = Icons.Filled.Psychology
        ),
        OnboardingStep(
            title = "Intelligence Setup",
            description = "Enter your API key and configuration. Your data stays local and private.",
            icon = Icons.Filled.Key,
            isLast = true
        )
    )

    val currentStep = steps[step]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = currentStep.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = currentStep.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = currentStep.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        if (step == 2) {
            Spacer(modifier = Modifier.height(32.dp))
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Model: $selectedModel")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    models.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                selectedModel = model
                                expanded = false
                                if (model.startsWith("gemini")) {
                                    baseUrl = ""
                                } else if (baseUrl.isBlank() || baseUrl.contains("google")) {
                                    baseUrl = "http://100.115.141.124:4891/v1/" // Default local bridge
                                }
                            }
                        )
                    }
                }
            }
        }

        if (currentStep.isLast) {
            Spacer(modifier = Modifier.height(32.dp))
            
            if (!selectedModel.startsWith("gemini")) {
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL (OpenAI Compatible)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                placeholder = { Text("Paste your key here...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null) }
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
                            if (index == step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(50)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                if (currentStep.isLast) {
                    if (apiKey.isNotBlank()) onConfigurationFinished(apiKey, selectedModel, baseUrl.takeIf { it.isNotBlank() })
                } else {
                    step++
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
                Text("Back", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
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
