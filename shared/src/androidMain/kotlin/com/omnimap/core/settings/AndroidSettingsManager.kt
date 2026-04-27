package com.omnimap.core.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

class AndroidSettingsManager(context: Context) : SettingsManager {
    private val prefs: SharedPreferences = context.getSharedPreferences("omnimap_settings", Context.MODE_PRIVATE)

    override fun getGeminiApiKey(): Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "gemini_api_key") {
                trySend(p.getString(key, null))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString("gemini_api_key", null))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun saveGeminiApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key).apply()
    }

    override fun isFirstLaunch(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "first_launch") {
                trySend(p.getBoolean(key, true))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getBoolean("first_launch", true))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean("first_launch", false).apply()
    }
}
