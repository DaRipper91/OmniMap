package com.omnimap.di

import com.omnimap.core.connectivity.ConnectivityObserver
import com.omnimap.core.connectivity.DesktopConnectivityObserver
import com.omnimap.core.haptics.DesktopHapticEngine
import com.omnimap.core.haptics.HapticEngine
import com.omnimap.core.settings.DesktopSettingsManager
import com.omnimap.core.settings.SettingsManager
import com.omnimap.data.repository.CliAiRepository
import com.omnimap.data.repository.DesktopAuthRepository
import com.omnimap.data.repository.FileOmniMapRepository
import com.omnimap.data.repository.GeminiRepositoryImpl
import com.omnimap.data.repository.ModelRoutingAiRepository
import com.omnimap.data.repository.OpenAiRepositoryImpl
import com.omnimap.domain.repository.AiInferenceRepository
import com.omnimap.domain.repository.AuthRepository
import com.omnimap.domain.repository.OmniMapRepository

class DesktopAppContainer {
    private val desktopSettings: DesktopSettingsManager by lazy {
        DesktopSettingsManager()
    }

    val settingsManager: SettingsManager get() = desktopSettings

    val hapticEngine: HapticEngine by lazy {
        DesktopHapticEngine()
    }

    val connectivityObserver: ConnectivityObserver by lazy {
        DesktopConnectivityObserver()
    }

    private val fileRepository: FileOmniMapRepository by lazy {
        FileOmniMapRepository()
    }

    val omniMapRepository: OmniMapRepository get() = fileRepository

    val authRepository: AuthRepository by lazy {
        DesktopAuthRepository()
    }

    val aiInferenceRepository: AiInferenceRepository by lazy {
        // Seed every client with the saved values so the app knows it is
        // configured on the very first frame and skips the setup wizard.
        val savedKey = desktopSettings.apiKey.value.orEmpty()
        val savedModel = desktopSettings.selectedModel.value
        ModelRoutingAiRepository(
            geminiRepo = GeminiRepositoryImpl(
                apiKey = savedKey,
                selectedModel = savedModel.takeIf { it.startsWith("gemini") } ?: "gemini-3.1-pro",
                settingsManager = desktopSettings
            ),
            openAiRepo = OpenAiRepositoryImpl(
                apiKey = savedKey,
                selectedModel = savedModel,
                baseUrl = desktopSettings.baseUrl.value,
                settingsManager = desktopSettings
            ),
            cliRepo = CliAiRepository(desktopSettings),
            settingsManager = desktopSettings,
            initialModel = savedModel
        )
    }

    /** Write anything not yet saved. Call before the app exits. */
    fun shutdown() {
        fileRepository.flush()
    }
}
