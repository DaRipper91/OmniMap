package com.omnimap.presentation.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
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

import com.omnimap.domain.repository.AuthRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    authRepository: AuthRepository,
    onConfigurationFinished: (String, String, String?) -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var apiKey by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("gemini-1.5-pro") }
    val scope = rememberCoroutineScope()
    val currentUser by authRepository.currentUser.collectAsState()
    var customModelName by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("https://api.openai.com/v1/") }
    var expanded by remember { mutableStateOf(false) }
    var isCustomModelSelected by remember { mutableStateOf(false) }

    val models = listOf(
        "gemini-1.5-pro-latest",
        "gemini-1.5-flash-latest",
        "gemini-2.0-flash",
        "gemini-2.0-pro-exp",
        "llama3.1",
        "qwen2.5",
        "Custom..."
    )

    val steps = listOf(
        OnboardingStep(
            title = "Your Mind, Visualized",
            description = "OmniMap transforms your fleeting thoughts into a persistent, native graph. Capture ideas, tasks, and complex goals with sub-millisecond tactile precision.",
            icon = Icons.Filled.AutoAwesome
        ),
        OnboardingStep(
            title = "The AI Architect",
            description = "Choose between Gemini 3.1, Llama 3.1, or Qwen 2.5. The Architect doesn't just chat—it proactively proposes mutations to expand and organize your mind-map.",
            icon = Icons.Filled.Psychology
        ),
        OnboardingStep(
            title = "Graph Mutations",
            description = "Experience real-time evolution. As you dialogue with the Architect, it drafts new nodes and connections, effectively building your workspace alongside you.",
            icon = Icons.Filled.Draw
        ),
        OnboardingStep(
            title = "Tactile Hardware Engine",
            description = "Optimized for 120Hz native execution. Feel every connection with deep haptic feedback integration, designed for immediate cognitive flow.",
            icon = Icons.Filled.Vibration
        ),
        OnboardingStep(
            title = "Privacy by Design",
            description = "Your graph data stays in a local Room database. Your API keys are stored only on this device. Total control, zero telemetry.",
            icon = Icons.Filled.Lock
        ),
        OnboardingStep(
            title = "Intelligence Setup",
            description = "Select your preferred brain and enter your API key to initialize the Mind-Map engine.",
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
        
        if (currentStep.isLast) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        authRepository.login().onSuccess { user ->
                            apiKey = user.accessToken ?: ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4285F4),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Login with Google")
            }

            if (currentUser != null) {
                Text(
                    text = "Logged in as ${currentUser?.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "OR ENTER API KEY MANUALLY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Model: ${if (isCustomModelSelected) "Custom" else selectedModel}")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    models.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                if (model == "Custom...") {
                                    isCustomModelSelected = true
                                } else {
                                    isCustomModelSelected = false
                                    selectedModel = model
                                }
                                expanded = false
                                
                                val modelToUse = if (isCustomModelSelected) customModelName else model
                                if (modelToUse.startsWith("gemini")) {
                                    baseUrl = ""
                                } else if (baseUrl.isBlank() || baseUrl.contains("google")) {
                                    baseUrl = "http://100.115.141.124:4891/v1/" // Default local bridge
                                }
                            }
                        )
                    }
                }
            }

            if (isCustomModelSelected) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = customModelName,
                    onValueChange = { customModelName = it },
                    label = { Text("Custom Model Name (e.g. gemini-3.1-pro)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val currentEffectiveModel = if (isCustomModelSelected) customModelName else selectedModel
            if (!currentEffectiveModel.startsWith("gemini")) {
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL (OpenAI Compatible)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
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
                leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
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
                    val finalModel = if (isCustomModelSelected) customModelName else selectedModel
                    if (apiKey.isNotBlank() && finalModel.isNotBlank()) {
                        onConfigurationFinished(apiKey, finalModel, baseUrl.takeIf { it.isNotBlank() })
                    }
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
            enabled = !currentStep.isLast || (apiKey.isNotBlank() && (!isCustomModelSelected || customModelName.isNotBlank()))
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

private data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isLast: Boolean = false
)
