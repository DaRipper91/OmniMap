package com.omnimap.data.remote.api

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Agent CLIs installed on the desktop (Claude Code, Codex, Copilot, Antigravity, OpenCode).
 * Model ids look like `cli:claude` or `cli:copilot:claude-sonnet-5.5` or `cli:opencode:opencode/big-pickle`;
 * requests are run through the CLI, so they use the CLI's own login and local app runtime.
 */
object AgentCli {
    const val PREFIX = "cli:"

    /** CLI command -> display name. */
    val all = linkedMapOf(
        "claude" to "Claude Code",
        "codex" to "Codex",
        "copilot" to "GitHub Copilot",
        "agy" to "Antigravity",
        "opencode" to "OpenCode"
    )

    val openCodeModels = listOf(
        "opencode/big-pickle",
        "opencode/nemotron-3.5-lightning-free",
        "opencode/deepseek-v4.1-flash-free",
        "opencode/beam-free",
        "opencode/space-bunny-free"
    )

    val copilotModels = listOf(
        "claude-sonnet-5.5",
        "claude-sonnet-5",
        "claude-opus-5.5",
        "gpt-6.1-sol",
        "gpt-5.4",
        "gemini-3.8-flash"
    )

    val claudeModels = listOf(
        "default",
        "sonnet",
        "haiku",
        "opus"
    )

    fun isCli(model: String): Boolean = model.startsWith(PREFIX)
    fun modelId(command: String, subModel: String? = null): String =
        if (subModel.isNullOrBlank() || subModel == "default") PREFIX + command else "$PREFIX$command:$subModel"
    fun command(model: String): String = model.removePrefix(PREFIX).substringBefore(':')
    fun subModel(model: String): String? = model.removePrefix(PREFIX).substringAfter(':', "").takeIf { it.isNotEmpty() }

    fun label(model: String): String {
        val cmd = command(model)
        val baseLabel = all[cmd] ?: cmd
        val sub = subModel(model)
        return if (sub != null) "$baseLabel ($sub)" else baseLabel
    }
}

/** Pre-configured model lists for Cloud providers. */
object CloudModelCatalogs {
    val gemini = listOf(
        "gemini-2.5-pro",
        "gemini-2.5-flash",
        "gemini-2.0-flash",
        "gemini-2.0-flash-lite",
        "gemini-1.5-pro",
        "gemini-1.5-flash"
    )

    val openAi = listOf(
        "gpt-4o",
        "gpt-4o-mini",
        "o3-mini",
        "o1",
        "o1-mini",
        "gpt-4-turbo"
    )

    val claude = listOf(
        "claude-3-7-sonnet",
        "claude-3-5-sonnet-20241022",
        "claude-3-5-haiku-20241022",
        "claude-3-opus-20240229"
    )

    val copilot = listOf(
        "claude-3.5-sonnet",
        "gpt-4o",
        "o1",
        "o3-mini"
    )
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
