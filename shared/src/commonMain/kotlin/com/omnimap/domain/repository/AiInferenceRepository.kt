package com.omnimap.domain.repository

interface AiInferenceRepository {
    suspend fun generateNodeSuggestion(contextPrompt: String): Result<String>
    suspend fun generateEmbedding(text: String): Result<List<Float>>
    fun isConfigured(): Boolean
    fun getSelectedModel(): String
    suspend fun saveConfiguration(apiKey: String, model: String, baseUrl: String?)
}