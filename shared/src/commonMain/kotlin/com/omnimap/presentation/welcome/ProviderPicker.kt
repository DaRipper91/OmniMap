package com.omnimap.presentation.welcome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.omnimap.data.remote.api.AgentCli
import com.omnimap.data.remote.api.CloudModelCatalogs
import com.omnimap.data.remote.api.LocalOllama
import kotlinx.coroutines.launch

enum class ProviderKind { AgentCli, Ollama, Cloud }
enum class CloudService(val label: String) {
    Gemini("Google Gemini"),
    OpenAi("OpenAI"),
    Claude("Anthropic Claude"),
    Copilot("GitHub Copilot"),
    OpenAiCompatible("Custom / Other")
}

/** What the user picked: the values [com.omnimap.domain.repository.AiInferenceRepository.saveConfiguration] takes. */
data class ProviderChoice(val apiKey: String, val model: String, val baseUrl: String?)

/** Selection state for [ProviderPicker], seeded from the saved configuration. */
@Stable
class ProviderPickerState(
    val agentClis: List<String>,
    initialModel: String?,
    initialBaseUrl: String?,
    initialApiKey: String?
) {
    var kind by mutableStateOf(
        when {
            initialModel != null && AgentCli.isCli(initialModel) -> ProviderKind.AgentCli
            LocalOllama.isOllama(initialBaseUrl) -> ProviderKind.Ollama
            initialModel == null && agentClis.isNotEmpty() -> ProviderKind.AgentCli
            else -> ProviderKind.Cloud
        }
    )
    var cli by mutableStateOf(initialModel?.takeIf(AgentCli::isCli) ?: agentClis.firstOrNull())
    var cliSubModel by mutableStateOf(initialModel?.let(AgentCli::subModel) ?: "default")

    var ollamaUrl by mutableStateOf(initialBaseUrl?.takeIf(LocalOllama::isOllama) ?: LocalOllama.BASE_URL)
    var ollamaModels by mutableStateOf(listOfNotNull(initialModel?.takeIf { LocalOllama.isOllama(initialBaseUrl) }))
    var ollamaModel by mutableStateOf(initialModel?.takeIf { LocalOllama.isOllama(initialBaseUrl) })
    var ollamaStatus by mutableStateOf<String?>(null)

    var cloud by mutableStateOf(
        when {
            initialModel?.startsWith("gemini") == true -> CloudService.Gemini
            initialModel?.startsWith("claude") == true && !AgentCli.isCli(initialModel) -> CloudService.Claude
            initialBaseUrl?.contains("githubcopilot") == true -> CloudService.Copilot
            initialBaseUrl?.contains("api.openai.com") == true ||
                initialModel?.startsWith("gpt-") == true ||
                initialModel?.startsWith("o1") == true ||
                initialModel?.startsWith("o3") == true -> CloudService.OpenAi
            initialBaseUrl != null && !LocalOllama.isOllama(initialBaseUrl) -> CloudService.OpenAiCompatible
            else -> CloudService.Gemini
        }
    )
    var geminiModel by mutableStateOf(initialModel?.takeIf { it in CloudModelCatalogs.gemini } ?: CloudModelCatalogs.gemini.first { it == "gemini-2.0-flash" })
    var openAiModel by mutableStateOf(initialModel?.takeIf { it in CloudModelCatalogs.openAi } ?: CloudModelCatalogs.openAi.first())
    var claudeModel by mutableStateOf(initialModel?.takeIf { it in CloudModelCatalogs.claude } ?: CloudModelCatalogs.claude.first())
    var copilotModel by mutableStateOf(initialModel?.takeIf { it in CloudModelCatalogs.copilot } ?: CloudModelCatalogs.copilot.first())

    var customUrl by mutableStateOf(initialBaseUrl?.takeIf { !LocalOllama.isOllama(it) } ?: "https://api.openai.com/v1/")
    var customModel by mutableStateOf(
        initialModel?.takeIf { cloud == CloudService.OpenAiCompatible && !it.startsWith("gemini") && !AgentCli.isCli(it) }.orEmpty()
    )
    var apiKey by mutableStateOf(initialApiKey.orEmpty())

    /** The complete choice, or null while something required is missing. */
    val choice: ProviderChoice?
        get() = when (kind) {
            ProviderKind.AgentCli -> cli?.let {
                val cmd = AgentCli.command(it)
                val fullModel = AgentCli.modelId(cmd, cliSubModel.takeIf { s -> s != "default" })
                ProviderChoice("", fullModel, null)
            }
            ProviderKind.Ollama -> ollamaModel?.let { ProviderChoice("", it, ollamaUrl) }
            ProviderKind.Cloud -> if (apiKey.isBlank()) null else when (cloud) {
                CloudService.Gemini -> ProviderChoice(apiKey, geminiModel, null)
                CloudService.OpenAi -> ProviderChoice(apiKey, openAiModel, "https://api.openai.com/v1/")
                CloudService.Claude -> ProviderChoice(apiKey, claudeModel, "https://api.anthropic.com/v1/")
                CloudService.Copilot -> ProviderChoice(apiKey, copilotModel, "https://api.githubcopilot.com/chat/completions")
                CloudService.OpenAiCompatible ->
                    if (customModel.isBlank() || customUrl.isBlank()) null else ProviderChoice(apiKey, customModel, customUrl)
            }
        }
}

@Composable
fun rememberProviderPickerState(
    agentClis: List<String>,
    initialModel: String? = null,
    initialBaseUrl: String? = null,
    initialApiKey: String? = null
) = remember(agentClis, initialModel, initialBaseUrl, initialApiKey) {
    ProviderPickerState(agentClis, initialModel, initialBaseUrl, initialApiKey)
}

/**
 * Three cards, one per provider type, with a radio-style selection. The selected card
 * expands to show its options. Used by first-run setup and by Settings.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProviderPicker(state: ProviderPickerState, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()

    suspend fun findOllamaModels() {
        state.ollamaStatus = "Looking for Ollama at ${state.ollamaUrl}…"
        val found = LocalOllama.fetchModels(state.ollamaUrl)
        state.ollamaModels = found
        if (found.isEmpty()) {
            state.ollamaStatus = "No models found. Is `ollama serve` running, and have you pulled a model?"
        } else {
            state.ollamaStatus = null
            if (state.ollamaModel !in found) state.ollamaModel = found.first()
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProviderCard(
            title = "Agent on this computer",
            subtitle = if (state.agentClis.isEmpty()) {
                "OpenCode, Claude Code, Codex, Copilot or Antigravity. None found on this device."
            } else {
                "Uses the agent CLI's local sign-in and execution environment. No API key needed."
            },
            icon = Icons.Filled.Terminal,
            selected = state.kind == ProviderKind.AgentCli,
            enabled = state.agentClis.isNotEmpty(),
            onSelect = { state.kind = ProviderKind.AgentCli }
        ) {
            Text("Installed Agent", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.agentClis.forEach { cli ->
                    val cmd = AgentCli.command(cli)
                    FilterChip(
                        selected = state.cli?.let(AgentCli::command) == cmd,
                        onClick = {
                            state.cli = cli
                            state.cliSubModel = "default"
                        },
                        label = { Text(AgentCli.all[cmd] ?: cmd) }
                    )
                }
            }

            val currentCmd = state.cli?.let(AgentCli::command)
            val subModels = when (currentCmd) {
                "opencode" -> listOf("default") + AgentCli.openCodeModels
                "copilot" -> listOf("default") + AgentCli.copilotModels
                "claude" -> AgentCli.claudeModels
                else -> emptyList()
            }

            if (subModels.isNotEmpty()) {
                Text("Model Variant", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    subModels.forEach { modelName ->
                        FilterChip(
                            selected = state.cliSubModel == modelName,
                            onClick = { state.cliSubModel = modelName },
                            label = { Text(modelName) }
                        )
                    }
                }
            }
        }

        ProviderCard(
            title = "Local models with Ollama",
            subtitle = "Runs on your machine. Nothing leaves it.",
            icon = Icons.Filled.Computer,
            selected = state.kind == ProviderKind.Ollama,
            onSelect = {
                state.kind = ProviderKind.Ollama
                if (state.ollamaModels.isEmpty()) scope.launch { findOllamaModels() }
            }
        ) {
            OutlinedTextField(
                value = state.ollamaUrl,
                onValueChange = { state.ollamaUrl = it },
                label = { Text("Ollama address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { scope.launch { findOllamaModels() } }) { Text("Find models") }
                state.ollamaStatus?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.ollamaModels.forEach { model ->
                    FilterChip(selected = state.ollamaModel == model, onClick = { state.ollamaModel = model }, label = { Text(model) })
                }
            }
        }

        ProviderCard(
            title = "Cloud API",
            subtitle = "Google Gemini, OpenAI, Anthropic Claude, GitHub Copilot, or custom endpoint.",
            icon = Icons.Filled.Cloud,
            selected = state.kind == ProviderKind.Cloud,
            onSelect = { state.kind = ProviderKind.Cloud }
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CloudService.entries.forEach { service ->
                    FilterChip(
                        selected = state.cloud == service,
                        onClick = { state.cloud = service },
                        label = { Text(service.label) }
                    )
                }
            }
            Text("Model", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
            when (state.cloud) {
                CloudService.Gemini -> FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CloudModelCatalogs.gemini.forEach { model ->
                        FilterChip(selected = state.geminiModel == model, onClick = { state.geminiModel = model }, label = { Text(model) })
                    }
                }
                CloudService.OpenAi -> FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CloudModelCatalogs.openAi.forEach { model ->
                        FilterChip(selected = state.openAiModel == model, onClick = { state.openAiModel = model }, label = { Text(model) })
                    }
                }
                CloudService.Claude -> FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CloudModelCatalogs.claude.forEach { model ->
                        FilterChip(selected = state.claudeModel == model, onClick = { state.claudeModel = model }, label = { Text(model) })
                    }
                }
                CloudService.Copilot -> FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CloudModelCatalogs.copilot.forEach { model ->
                        FilterChip(selected = state.copilotModel == model, onClick = { state.copilotModel = model }, label = { Text(model) })
                    }
                }
                CloudService.OpenAiCompatible -> {
                    OutlinedTextField(
                        value = state.customUrl,
                        onValueChange = { state.customUrl = it },
                        label = { Text("Base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.customModel,
                        onValueChange = { state.customModel = it },
                        label = { Text("Model name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = { state.apiKey = it },
                label = { Text(if (state.cloud == CloudService.Copilot) "GitHub Token" else "API key") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null) },
                supportingText = { Text("Stored only on this device.") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * A selectable provider card. Selected: 2dp primary border, tinted fill, filled radio,
 * and its options shown below the header. Disabled cards stay visible but muted.
 */
@Composable
private fun ProviderCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onSelect,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        border = if (selected) BorderStroke(2.dp, colors.primary) else BorderStroke(1.dp, colors.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) colors.primaryContainer.copy(alpha = 0.35f) else colors.surfaceContainerLow,
            disabledContainerColor = colors.surfaceContainerLow.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selected, onClick = onSelect, enabled = enabled)
                Icon(icon, contentDescription = null, tint = if (enabled) colors.primary else colors.onSurface.copy(alpha = 0.4f))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurface.copy(alpha = 0.75f))
                }
            }
            if (selected) {
                Column(
                    modifier = Modifier.padding(start = 48.dp, top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = content
                )
            }
        }
    }
}
