package com.omnimap.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.omnimap.core.settings.SettingsManager
import com.omnimap.domain.repository.AiInferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GeminiRepositoryImpl(
    private var apiKey: String,
    private var selectedModel: String = "gemini-1.5-pro",
    private val settingsManager: SettingsManager
) : AiInferenceRepository {

    private val modelFlow = MutableStateFlow<GenerativeModel?>(createModel(apiKey, selectedModel))
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        settingsManager.getGeminiApiKey()
            .onEach { newKey ->
                if (newKey != null && newKey != apiKey) {
                    apiKey = newKey
                    modelFlow.value = createModel(newKey, selectedModel)
                }
            }
            .launchIn(scope)
            
        settingsManager.getSelectedModel()
            .onEach { newModel ->
                if (newModel != selectedModel && newModel.startsWith("gemini")) {
                    selectedModel = newModel
                    modelFlow.value = createModel(apiKey, newModel)
                }
            }
            .launchIn(scope)
    }

    private fun createModel(key: String, modelName: String): GenerativeModel? {
        return if (key.isBlank() || key.startsWith("REPLACE_WITH") || key == "dummy_key_for_build") {
            null
        } else {
            GenerativeModel(
                modelName = modelName,
                apiKey = key,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                }
            )
        }
    }

    override fun isConfigured(): Boolean = modelFlow.value != null
    
    override fun getSelectedModel(): String = selectedModel

    suspend fun saveApiKey(key: String) {
        settingsManager.saveGeminiApiKey(key)
    }
    
    override suspend fun saveConfiguration(apiKey: String, model: String, baseUrl: String?) {
        settingsManager.saveGeminiApiKey(apiKey)
        settingsManager.saveSelectedModel(model)
        settingsManager.saveBaseUrl(baseUrl)
    }

    override suspend fun generateNodeSuggestion(contextPrompt: String): Result<String> {
        val currentModel = modelFlow.value ?: return Result.failure(Exception("Gemini API Key not configured. Please use the setup wizard."))
        
        return try {
            val response = currentModel.generateContent(contextPrompt)
            val text = response.text ?: return Result.failure(Exception("Empty response from Gemini"))
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
