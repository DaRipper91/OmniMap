package com.omnimap.presentation.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.omnimap.domain.repository.AuthRepository
import kotlinx.coroutines.launch

private data class IntroStep(val title: String, val description: String, val icon: ImageVector)

private val introSteps = listOf(
    IntroStep(
        title = "Your mind, mapped",
        description = "OmniMap is a graph of your goals, projects, tasks and ideas, linked the way you think. " +
            "It lives on this device: no account, no telemetry.",
        icon = Icons.Filled.Hub
    ),
    IntroStep(
        title = "The Architect drafts. You decide.",
        description = "Ask the AI Architect to break down a goal or find what's missing. Its suggestions appear as " +
            "dashed drafts on your map that you commit or discard.",
        icon = Icons.Filled.Psychology
    )
)

/**
 * First-run flow: two short intro steps (skippable), then choosing an AI provider.
 * The action bar is pinned to the bottom so Finish is always visible.
 */
@Composable
fun WelcomeScreen(
    authRepository: AuthRepository,
    agentClis: List<String> = emptyList(),
    onConfigurationFinished: (String, String, String?) -> Unit
) {
    var step by remember { mutableStateOf(0) }
    val setupStep = introSteps.size
    val isSetup = step == setupStep
    val picker = rememberProviderPickerState(agentClis)
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isSetup) {
                    val intro = introSteps[step]
                    Spacer(Modifier.height(48.dp))
                    Icon(intro.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(88.dp))
                    Spacer(Modifier.height(32.dp))
                    Text(
                        intro.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        intro.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                    )
                } else {
                    Text(
                        "Choose the Architect's brain",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "You can change this any time in Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp)
                    )
                    ProviderPicker(picker, Modifier.fillMaxWidth())

                    if (authRepository.isLoginAvailable) {
                        TextButton(
                            onClick = { scope.launch { authRepository.login().onSuccess { picker.apiKey = it.accessToken.orEmpty() } } },
                            modifier = Modifier.padding(top = 8.dp)
                        ) { Text("Sign in with Google instead") }
                    }
                }
            }
        }

        // Pinned action bar
        Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 2.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    TextButton(onClick = { step-- }) { Text("Back") }
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    (0..setupStep).forEach { index ->
                        Box(
                            Modifier
                                .size(if (index == step) 10.dp else 8.dp)
                                .background(
                                    if (index == step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(50)
                                )
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                if (!isSetup) {
                    TextButton(onClick = { step = setupStep }) { Text("Skip") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { step++ }) { Text("Next") }
                } else {
                    val choice = picker.choice
                    Button(
                        onClick = { choice?.let { onConfigurationFinished(it.apiKey, it.model, it.baseUrl) } },
                        enabled = choice != null
                    ) { Text("Finish setup", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
