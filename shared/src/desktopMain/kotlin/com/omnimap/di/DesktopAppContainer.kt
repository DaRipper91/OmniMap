package com.omnimap.di

import com.omnimap.core.connectivity.ConnectivityObserver
import com.omnimap.core.connectivity.DesktopConnectivityObserver
import com.omnimap.core.haptics.DesktopHapticEngine
import com.omnimap.core.haptics.HapticEngine
import com.omnimap.core.settings.DesktopSettingsManager
import com.omnimap.core.settings.SettingsManager
import com.omnimap.data.repository.DesktopAuthRepository
import com.omnimap.data.repository.GeminiRepositoryImpl
import com.omnimap.data.repository.MockOmniMapRepository
import com.omnimap.domain.repository.AiInferenceRepository
import com.omnimap.domain.repository.AuthRepository
import com.omnimap.domain.repository.OmniMapRepository

class DesktopAppContainer {
    val settingsManager: SettingsManager by lazy {
        DesktopSettingsManager()
    }

    val hapticEngine: HapticEngine by lazy {
        DesktopHapticEngine()
    }

    val connectivityObserver: ConnectivityObserver by lazy {
        DesktopConnectivityObserver()
    }

    val omniMapRepository: OmniMapRepository by lazy {
        MockOmniMapRepository()
    }

    val authRepository: AuthRepository by lazy {
        DesktopAuthRepository()
    }

    val aiInferenceRepository: AiInferenceRepository by lazy {
        GeminiRepositoryImpl(
            apiKey = "",
            settingsManager = settingsManager
        )
    }
}
