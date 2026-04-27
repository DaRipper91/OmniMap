package com.omnimap.core.settings

import kotlinx.coroutines.flow.Flow

interface SettingsManager {
    fun getGeminiApiKey(): Flow<String?>
    suspend fun saveGeminiApiKey(key: String)
    fun isFirstLaunch(): Flow<Boolean>
    suspend fun setFirstLaunchCompleted()
}
