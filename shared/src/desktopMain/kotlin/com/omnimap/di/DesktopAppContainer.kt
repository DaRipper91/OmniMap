package com.omnimap.di

import com.omnimap.core.haptics.DesktopHapticEngine
import com.omnimap.core.haptics.HapticEngine
import com.omnimap.data.repository.GeminiRepositoryImpl
import com.omnimap.data.repository.MockOmniMapRepository
import com.omnimap.domain.repository.AiInferenceRepository
import com.omnimap.domain.repository.OmniMapRepository

class DesktopAppContainer {
    val hapticEngine: HapticEngine by lazy {
        DesktopHapticEngine()
    }

    val omniMapRepository: OmniMapRepository by lazy {
        MockOmniMapRepository()
    }

    val aiInferenceRepository: AiInferenceRepository by lazy {
        // REPLACE_WITH_GEMINI_API_KEY
        GeminiRepositoryImpl(apiKey = "")
    }
}
