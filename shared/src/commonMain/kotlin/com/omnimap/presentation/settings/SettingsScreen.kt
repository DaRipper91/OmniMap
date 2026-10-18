package com.omnimap.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omnimap.core.settings.SettingsManager
import com.omnimap.presentation.welcome.ProviderChoice
import com.omnimap.presentation.welcome.ProviderPicker
import com.omnimap.presentation.welcome.rememberProviderPickerState

/**
 * Change the AI provider after first-run setup. Reuses the setup picker, seeded with
 * the saved provider, model, address and key.
 */
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager?,
    agentClis: List<String>,
    onSave: (ProviderChoice) -> Unit
) {
    val savedKey by (settingsManager?.getGeminiApiKey() ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(null)
    val savedModel by (settingsManager?.getSelectedModel() ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(null)
    val savedUrl by (settingsManager?.getBaseUrl() ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(null)
    val picker = rememberProviderPickerState(agentClis, savedModel, savedUrl, savedKey)
    var justSaved by remember { mutableStateOf(false) }
    val choice = picker.choice

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp)) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Text("AI provider", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(
                "Which model the Architect uses for drafting nodes and links.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )
            ProviderPicker(picker, Modifier.fillMaxWidth())
            Row(Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { choice?.let { onSave(it); justSaved = true } },
                    enabled = choice != null
                ) { Text("Save") }
                if (justSaved) {
                    Text(
                        "Saved. New requests use this provider.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 32.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Text("Your data", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(
                "Your graph and keys are stored only on this device. Use Export on the Search screen to back up your graph.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
