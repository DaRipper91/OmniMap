package com.omnimap.core.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DesktopSettingsManager : SettingsManager {
    private val _apiKey = MutableStateFlow<String?>(null)
    private val _firstLaunch = MutableStateFlow(true)

    override fun getGeminiApiKey(): Flow<String?> = _apiKey.asStateFlow()

    override suspend fun saveGeminiApiKey(key: String) {
        _apiKey.value = key
    }

    override fun isFirstLaunch(): Flow<Boolean> = _firstLaunch.asStateFlow()

    override suspend fun setFirstLaunchCompleted() {
        _firstLaunch.value = false
    }
}
