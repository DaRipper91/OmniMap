package com.omnimap.domain.repository

import com.omnimap.data.remote.dto.OllamaResponse

interface AiInferenceRepository {
    suspend fun generateNodeSuggestion(contextPrompt: String): Result<String>
}