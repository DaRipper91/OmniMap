package com.omnimap.data.remote.api

import com.omnimap.data.remote.dto.OllamaRequest
import com.omnimap.data.remote.dto.OllamaResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaApi {
    @POST("api/generate")
    suspend fun generateSuggestion(@Body request: OllamaRequest): OllamaResponse
}