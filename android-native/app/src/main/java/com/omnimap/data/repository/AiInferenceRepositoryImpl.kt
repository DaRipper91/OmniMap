package com.omnimap.data.repository

import com.omnimap.data.remote.api.OllamaApi
import com.omnimap.data.remote.dto.OllamaRequest
import com.omnimap.domain.repository.AiInferenceRepository

class AiInferenceRepositoryImpl(
    private val api: OllamaApi
) : AiInferenceRepository {

    override suspend fun generateNodeSuggestion(contextPrompt: String): Result<String> {
        return try {
            val response = api.generateSuggestion(
                OllamaRequest(prompt = contextPrompt)
            )
            Result.success(response.response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}