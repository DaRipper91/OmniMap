package com.omnimap.data.remote.dto

data class OllamaRequest(
    val model: String = "omnimap-architect",
    val prompt: String,
    val stream: Boolean = false,
    val format: String = "json"
)

data class OllamaResponse(
    val response: String,
    val done: Boolean
)