package com.omnimap.core.settings

import kotlinx.coroutines.flow.Flow

interface SettingsManager {
    fun getGeminiApiKey(): Flow<String?>
    suspend fun saveGeminiApiKey(key: String)
    
    fun getSelectedModel(): Flow<String>
    suspend fun saveSelectedModel(model: String)

    fun getBaseUrl(): Flow<String?>
    suspend fun saveBaseUrl(url: String?)

    fun isFirstLaunch(): Flow<Boolean>
    suspend fun setFirstLaunchCompleted()
}
