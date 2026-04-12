package com.omnimap.data.repository

import com.omnimap.domain.repository.AiInferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class DesktopAiInferenceRepositoryImpl : AiInferenceRepository {

    override suspend fun generateNodeSuggestion(contextPrompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://100.115.141.124:4891/api/generate")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            // Escape quotes and newlines in the prompt for valid JSON
            val escapedPrompt = contextPrompt.replace("\"", "\\\"").replace("\n", "\\n")
            val jsonInputString = """
                {
                    "model": "omnimap-architect",
                    "prompt": "$escapedPrompt",
                    "stream": false,
                    "format": "json"
                }
            """.trimIndent()

            connection.outputStream.use {
                val input = jsonInputString.toByteArray(Charsets.UTF_8)
                it.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                // Basic JSON parsing without an external library just for desktop prototype
                val responseText = response.substringAfter("\"response\":").substringBefore(",\"done\"").replace("\\n", "\n").replace("\\\"", "\"")
                Result.success(responseText)
            } else {
                Result.failure(Exception("HTTP Error: $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
