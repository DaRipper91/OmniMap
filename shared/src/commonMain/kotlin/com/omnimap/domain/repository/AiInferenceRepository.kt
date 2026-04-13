package com.omnimap.domain.repository

interface AiInferenceRepository {
    suspend fun generateNodeSuggestion(contextPrompt: String): Result<String>
}