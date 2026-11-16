package com.omnimap.data.repository

import com.google.gson.Gson
import com.omnimap.core.settings.SettingsManager
import com.omnimap.domain.repository.AiInferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AnthropicRepositoryImpl(
    private var apiKey: String,
    private var selectedModel: String,
    private val settingsManager: SettingsManager
) : AiInferenceRepository {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        settingsManager.getGeminiApiKey().onEach { apiKey = it ?: "" }.launchIn(scope)
        settingsManager.getSelectedModel().onEach { selectedModel = it }.launchIn(scope)
    }

    override fun isConfigured(): Boolean = apiKey.isNotBlank()

    override fun getSelectedModel(): String = selectedModel

    override suspend fun saveConfiguration(apiKey: String, model: String, baseUrl: String?) {
        settingsManager.saveGeminiApiKey(apiKey)
        settingsManager.saveSelectedModel(model)
        settingsManager.saveBaseUrl(baseUrl ?: "https://api.anthropic.com/v1/")
    }

    override suspend fun generateEmbedding(text: String): Result<List<Float>> = Result.success(emptyList())

    override suspend fun generateNodeSuggestion(contextPrompt: String): Result<String> {
        val requestBody = mapOf(
            "model" to selectedModel,
            "max_tokens" to 4096,
            "system" to "You are a graph architect. Output ONLY valid JSON matching: { \"nodes\": [...], \"edges\": [...], \"message\": \"...\" }.",
            "messages" to listOf(
                mapOf("role" to "user", "content" to contextPrompt)
            )
        )

        val body = gson.toJson(requestBody).toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    val responseText = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception("Anthropic API Error: ${response.code} ${response.message} ${responseText.take(300)}")
                        )
                    }
                    val json = gson.fromJson(responseText, Map::class.java)
                    val contentList = json["content"] as? List<*>
                    val firstItem = contentList?.firstOrNull() as? Map<*, *>
                    val text = firstItem?.get("text") as? String
                    if (text != null) {
                        Result.success(text)
                    } else {
                        Result.failure(Exception("Malformed response from Anthropic"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
