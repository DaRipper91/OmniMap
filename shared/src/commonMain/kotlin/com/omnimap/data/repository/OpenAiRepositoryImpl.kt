package com.omnimap.data.repository

import com.omnimap.core.settings.SettingsManager
import com.omnimap.data.remote.api.requiresApiKey
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

    override fun isConfigured(): Boolean =
        !baseUrl.isNullOrBlank() && (apiKey.isNotBlank() || !requiresApiKey(selectedModel, baseUrl))

    override fun getSelectedModel(): String = selectedModel

    override suspend fun saveConfiguration(apiKey: String, model: String, baseUrl: String?) {
        settingsManager.saveGeminiApiKey(apiKey)
        settingsManager.saveSelectedModel(model)
        settingsManager.saveBaseUrl(baseUrl)
    }

    override suspend fun generateEmbedding(text: String): Result<List<Float>> {
        // Mocking for now, as local providers like Ollama often have separate /embeddings endpoints
        return Result.success(emptyList())
    }

    override suspend fun generateNodeSuggestion(contextPrompt: String): Result<String> {
        val url = baseUrl ?: return Result.failure(Exception("Base URL not configured"))
        val finalUrl = if (url.endsWith("/")) "${url}chat/completions" else "$url/chat/completions"

        val result = postChatCompletion(finalUrl, contextPrompt, jsonMode = true)
        // Not every OpenAI-compatible model supports response_format (several of
        // OpenCode Zen's free models don't). Retry once without it; GraphViewModel
        // already extracts JSON from fenced or prose-wrapped replies.
        val status = (result.exceptionOrNull() as? ApiException)?.code
        return if (status == 400 || status == 422) {
            postChatCompletion(finalUrl, contextPrompt, jsonMode = false)
        } else {
            result
        }
    }

    private class ApiException(val code: Int, message: String) : Exception(message)

    private suspend fun postChatCompletion(url: String, contextPrompt: String, jsonMode: Boolean): Result<String> {
        val requestBody = buildMap<String, Any> {
            put("model", selectedModel)
            put(
                "messages", listOf(
                    mapOf("role" to "system", "content" to "You are a graph architect. Output ONLY valid JSON."),
                    mapOf("role" to "user", "content" to contextPrompt)
                )
            )
            if (jsonMode) put("response_format", mapOf("type" to "json_object"))
        }

        val body = gson.toJson(requestBody).toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val detail = response.body?.string()?.take(300).orEmpty()
                        return@withContext Result.failure(
                            ApiException(response.code, "API Error: ${response.code} ${response.message} $detail".trim())
                        )
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
