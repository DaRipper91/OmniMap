package com.omnimap.data.repository

import com.omnimap.core.settings.DesktopSettingsManager
import com.omnimap.data.remote.api.AgentCli
import com.omnimap.domain.repository.AiInferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit

/**
 * Runs the selected agent CLI (`cli:claude`, `cli:codex`, …) in one-shot print mode
 * from an empty temp directory, with tools off or read-only, and returns its stdout.
 */
class CliAiRepository(private val settings: DesktopSettingsManager) : AiInferenceRepository {

    private fun args(command: String, prompt: String): List<String> = when (command) {
        "claude" -> listOf("-p", prompt, "--tools", "")
        "codex" -> listOf("exec", "--skip-git-repo-check", "--ephemeral", "-s", "read-only", prompt)
        "copilot" -> listOf("-p", prompt, "-s")
        "agy" -> listOf("-p", prompt, "--mode", "plan")
        else -> error("Unknown CLI: $command")
    }

    override suspend fun generateNodeSuggestion(contextPrompt: String): Result<String> = withContext(Dispatchers.IO) {
        val command = AgentCli.command(settings.selectedModel.value)
        val exe = find(command) ?: return@withContext Result.failure(Exception("$command is not installed"))
        val prompt = "You are a graph architect. Output ONLY valid JSON.\n\n$contextPrompt"
        // ponytail: prompt goes in argv (128 KB per-arg limit on Linux); switch to stdin if graphs get that big.
        val dir = Files.createTempDirectory("omnimap-cli").toFile()
        try {
            val out = File(dir, ".out")
            val err = File(dir, ".err")
            val process = ProcessBuilder(listOf(exe.path) + args(command, prompt))
                .directory(dir)
                .redirectOutput(out)
                .redirectError(err)
                .apply {
                    // Launched from a desktop menu, PATH may miss the CLI's dir (and node, for codex).
                    environment()["PATH"] = (listOf(exe.parent) + searchPath).distinct().joinToString(File.pathSeparator)
                }
                .start()
            process.outputStream.close()
            if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                process.destroyForcibly()
                return@withContext Result.failure(Exception("$command timed out after $TIMEOUT_MINUTES minutes"))
            }
            if (process.exitValue() != 0) {
                Result.failure(Exception("$command exited ${process.exitValue()}: ${err.readText().takeLast(300).trim()}"))
            } else {
                Result.success(out.readText().trim())
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            dir.deleteRecursively()
        }
    }

    override suspend fun generateEmbedding(text: String): Result<List<Float>> = Result.success(emptyList())

    override fun isConfigured(): Boolean = find(AgentCli.command(settings.selectedModel.value)) != null

    override fun getSelectedModel(): String = settings.selectedModel.value

    override suspend fun saveConfiguration(apiKey: String, model: String, baseUrl: String?) {
        settings.saveSelectedModel(model)
    }

    companion object {
        private const val TIMEOUT_MINUTES = 5L
        private val home = System.getProperty("user.home")

        // ponytail: Linux/macOS layout only; Windows .cmd shims aren't searched.
        private val searchPath: List<String> =
            System.getenv("PATH").orEmpty().split(File.pathSeparator).filter { it.isNotBlank() } +
                listOf("$home/.local/bin", "$home/.npm-global/bin", "$home/.bun/bin", "/usr/local/bin", "/usr/bin")

        private fun find(command: String): File? =
            searchPath.map { File(it, command) }.firstOrNull { it.isFile && it.canExecute() }

        /** Agent CLIs found on this machine, as `cli:<command>` model ids. */
        fun installed(): List<String> = AgentCli.all.keys.filter { find(it) != null }.map(AgentCli::modelId)
    }
}
