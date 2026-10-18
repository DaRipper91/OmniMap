package com.omnimap.data.repository

import com.omnimap.core.settings.SettingsManager
import com.omnimap.data.remote.api.AgentCli
import com.omnimap.domain.repository.AiInferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Sends each request to Gemini when the selected model is a Gemini model,
 * and to the OpenAI-compatible client (OpenCode Zen, Ollama, OpenAI…) otherwise.
 */
class ModelRoutingAiRepository(
    private val geminiRepo: AiInferenceRepository,
    private val openAiRepo: AiInferenceRepository,
    /** Desktop only: runs installed agent CLIs for `cli:*` models. */
    private val cliRepo: AiInferenceRepository? = null,
    private val settingsManager: SettingsManager,
    initialModel: String = "gemini-3.1-pro"
) : AiInferenceRepository {

    private var currentModel: String = initialModel
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        settingsManager.getSelectedModel()
            .onEach { currentModel = it }
            .launchIn(scope)
    }

    private fun getActiveRepo(): AiInferenceRepository = when {
        currentModel.startsWith("gemini") -> geminiRepo
        cliRepo != null && AgentCli.isCli(currentModel) -> cliRepo
        else -> openAiRepo
    }

    override suspend fun generateNodeSuggestion(contextPrompt: String): Result<String> =
        getActiveRepo().generateNodeSuggestion(contextPrompt)

    override suspend fun generateEmbedding(text: String): Result<List<Float>> =
        getActiveRepo().generateEmbedding(text)

    override fun isConfigured(): Boolean = getActiveRepo().isConfigured()

    override fun getSelectedModel(): String = currentModel

    override suspend fun saveConfiguration(apiKey: String, model: String, baseUrl: String?) {
        // Switch immediately so the next request goes to the right provider,
        // without waiting for the settings flow to emit.
        currentModel = model
        settingsManager.saveGeminiApiKey(apiKey)
        settingsManager.saveSelectedModel(model)
        settingsManager.saveBaseUrl(baseUrl)
    }
}
