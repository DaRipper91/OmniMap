package com.omnimap.data.repository

import com.omnimap.core.settings.SettingsManager
import com.omnimap.domain.repository.AiInferenceRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OpenAiRepositoryImpl(
    private var apiKey: String,
    private var selectedModel: String,
    private var baseUrl: String?,
    private val settingsManager: SettingsManager
) : AiInferenceRepository {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        settingsManager.getGeminiApiKey().onEach { apiKey = it ?: "" }.launchIn(scope)
        settingsManager.getSelectedModel().onEach { selectedModel = it }.launchIn(scope)
        settingsManager.getBaseUrl().onEach { baseUrl = it }.launchIn(scope)
    }

    override fun isConfigured(): Boolean = apiKey.isNotBlank() && !baseUrl.isNullOrBlank()

    override fun getSelectedModel(): String = selectedModel

    override suspend fun saveConfiguration(apiKey: String, model: String, baseUrl: String?) {
        settingsManager.saveGeminiApiKey(apiKey)
        settingsManager.saveSelectedModel(model)
        settingsManager.saveBaseUrl(baseUrl)
    }

    override suspend fun generateNodeSuggestion(contextPrompt: String): Result<String> {
        val url = baseUrl ?: return Result.failure(Exception("Base URL not configured"))
        val finalUrl = if (url.endsWith("/")) "${url}chat/completions" else "$url/chat/completions"

        val requestBody = mapOf(
            "model" to selectedModel,
            "messages" to listOf(
                mapOf("role" to "system", "content" to "You are a graph architect. Output ONLY valid JSON."),
                mapOf("role" to "user", "content" to contextPrompt)
            ),
            "response_format" to mapOf("type" to "json_object")
        )

        val body = gson.toJson(requestBody).toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(finalUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("API Error: ${response.code} ${response.message}"))
                    }
                    val responseText = response.body?.string() ?: ""
                    val jsonResponse = gson.fromJson(responseText, Map::class.java)
                    val choices = jsonResponse["choices"] as List<*>
                    val firstChoice = choices[0] as Map<*, *>
                    val message = firstChoice["message"] as Map<*, *>
                    val content = message["content"] as String
                    Result.success(content)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
