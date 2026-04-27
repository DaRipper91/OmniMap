package com.omnimap.core.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DesktopSettingsManager : SettingsManager {
    private val _apiKey = MutableStateFlow<String?>(null)
    private val _selectedModel = MutableStateFlow("gemini-1.5-pro")
    private val _baseUrl = MutableStateFlow<String?>(null)
    private val _firstLaunch = MutableStateFlow(true)

    override fun getGeminiApiKey(): Flow<String?> = _apiKey.asStateFlow()

    override suspend fun saveGeminiApiKey(key: String) {
        _apiKey.value = key
    }

    override fun getSelectedModel(): Flow<String> = _selectedModel.asStateFlow()

    override suspend fun saveSelectedModel(model: String) {
        _selectedModel.value = model
    }

    override fun getBaseUrl(): Flow<String?> = _baseUrl.asStateFlow()

    override suspend fun saveBaseUrl(url: String?) {
        _baseUrl.value = url
    }

    override fun isFirstLaunch(): Flow<Boolean> = _firstLaunch.asStateFlow()

    override suspend fun setFirstLaunchCompleted() {
        _firstLaunch.value = false
    }
}
