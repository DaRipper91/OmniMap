package com.omnimap.data.remote.api

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * OpenCode Zen (https://opencode.ai/docs/zen/): an OpenAI-compatible gateway
 * whose catalogue includes a rotating set of free models.
 */
object OpenCodeZen {
    const val BASE_URL = "https://opencode.ai/inference/openai/v1/"
    private const val MODELS_URL = "https://opencode.ai/inference/v1/models"

    /** Used when the model list can't be fetched (offline, endpoint changed). */
    val fallbackFreeModels = listOf(
        "big-pickle",
        "glm-5.3-flash-free",
        "deepseek-v4.1-flash-free",
        "nemotron-3.5-lightning-free"
    )

    fun isZenUrl(url: String?): Boolean = url?.contains("opencode.ai") == true

    private fun isFree(id: String): Boolean = id.endsWith("-free") || id == "big-pickle"

    /**
     * Fetches the current free models. The free line-up changes often, so this
     * is read at setup time rather than hard-coded.
     */
    suspend fun fetchFreeModels(client: OkHttpClient = OkHttpClient()): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(MODELS_URL).get().build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext fallbackFreeModels
                    val body = response.body?.string().orEmpty()
                    val parsed = Gson().fromJson(body, Map::class.java)
                    val ids = (parsed?.get("data") as? List<*>)
                        ?.mapNotNull { (it as? Map<*, *>)?.get("id") as? String }
                        ?.filter(::isFree)
                        .orEmpty()
                    ids.ifEmpty { fallbackFreeModels }
                }
            } catch (e: Exception) {
                fallbackFreeModels
            }
        }
}
