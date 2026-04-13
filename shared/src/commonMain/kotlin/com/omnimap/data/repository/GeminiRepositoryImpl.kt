package com.omnimap.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.omnimap.domain.repository.AiInferenceRepository

class GeminiRepositoryImpl(
    private val apiKey: String
) : AiInferenceRepository {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-pro",
        apiKey = apiKey,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    override suspend fun generateNodeSuggestion(contextPrompt: String): Result<String> {
        return try {
            val response = model.generateContent(contextPrompt)
            val text = response.text ?: return Result.failure(Exception("Empty response from Gemini"))
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
