package com.omnimap.presentation.graph.chat

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    /** A failed request; shown in the error style with a Retry action. */
    val isError: Boolean = false
)