package com.omnimap.data.remote.api

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Agent CLIs installed on the desktop (Claude Code, Codex, Copilot, Antigravity).
 * Model ids look like `cli:claude`; requests are run through the CLI, so they use
 * the CLI's own login instead of an API key.
 */
object AgentCli {
    const val PREFIX = "cli:"

    /** CLI command -> display name. */
    val all = linkedMapOf(
        "claude" to "Claude Code",
        "codex" to "Codex",
        "copilot" to "GitHub Copilot",
        "agy" to "Antigravity"
    )

    fun isCli(model: String): Boolean = model.startsWith(PREFIX)
    fun modelId(command: String): String = PREFIX + command
    fun command(model: String): String = model.removePrefix(PREFIX)
    fun label(model: String): String = all[command(model)] ?: model
}

/** Ollama's OpenAI-compatible endpoint. Local models need no API key. */
object LocalOllama {
    const val BASE_URL = "http://localhost:11434/v1/"

    fun isOllama(url: String?): Boolean =
        url != null && (url.contains("localhost") || url.contains("127.0.0.1") || url.contains(":11434"))

    /** Installed models from `/api/tags`, or empty if Ollama isn't reachable at [baseUrl]. */
    suspend fun fetchModels(baseUrl: String = BASE_URL, client: OkHttpClient = OkHttpClient()): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val root = baseUrl.trimEnd('/').removeSuffix("/v1")
                val request = Request.Builder().url("$root/api/tags").get().build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext emptyList()
                    val parsed = Gson().fromJson(response.body?.string().orEmpty(), Map::class.java)
                    (parsed?.get("models") as? List<*>)
                        ?.mapNotNull { (it as? Map<*, *>)?.get("name") as? String }
                        .orEmpty()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
}

/** Agent CLIs and Ollama work without a key; everything else needs one. */
fun requiresApiKey(model: String, baseUrl: String?): Boolean =
    !AgentCli.isCli(model) && !LocalOllama.isOllama(baseUrl)
