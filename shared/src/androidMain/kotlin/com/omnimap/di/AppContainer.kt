package com.omnimap.di

import android.content.Context
import androidx.room.Room
import com.omnimap.core.connectivity.ConnectivityObserver
import com.omnimap.core.connectivity.NetworkConnectivityObserver
import com.omnimap.core.haptics.AndroidHapticEngine
import com.omnimap.core.haptics.HapticEngine
import com.omnimap.core.settings.AndroidSettingsManager
import com.omnimap.core.settings.SettingsManager
import com.omnimap.data.local.db.OmniMapDatabase
import com.omnimap.data.remote.api.OllamaApi
import com.omnimap.data.repository.GeminiRepositoryImpl
import com.omnimap.data.repository.OmniMapRepositoryImpl
import com.omnimap.domain.repository.AiInferenceRepository
import com.omnimap.domain.repository.OmniMapRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(private val context: Context) {
    val settingsManager: SettingsManager by lazy {
        AndroidSettingsManager(context)
    }

    val database: OmniMapDatabase by lazy {
        Room.databaseBuilder(
            context,
            OmniMapDatabase::class.java,
            "omnimap_db"
        ).fallbackToDestructiveMigration().build()
    }

    val hapticEngine: HapticEngine by lazy {
        AndroidHapticEngine(context)
    }

    val connectivityObserver: ConnectivityObserver by lazy {
        NetworkConnectivityObserver(context)
    }

    val ollamaApi: OllamaApi by lazy {
        Retrofit.Builder()
            // Default IP pointing to the local gpt4all/ollama bridge as defined in ARCH_LIVE_INTELLIGENCE.md
            .baseUrl("http://100.115.141.124:4891/") 
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApi::class.java)
    }

    val omniMapRepository: OmniMapRepository by lazy {
        OmniMapRepositoryImpl(
            nodeDao = database.nodeDao(),
            edgeDao = database.edgeDao(),
            queuedAiRequestDao = database.queuedAiRequestDao()
        )
    }

    val authRepository: com.omnimap.domain.repository.AuthRepository by lazy {
        com.omnimap.data.repository.AndroidAuthRepository(context)
    }

    val aiInferenceRepository: AiInferenceRepository by lazy {
        object : AiInferenceRepository {
            private val geminiRepo = GeminiRepositoryImpl(
                apiKey = runBlocking { settingsManager.getGeminiApiKey().firstOrNull() } ?: "",
                selectedModel = runBlocking { settingsManager.getSelectedModel().firstOrNull() } ?: "gemini-1.5-pro",
                settingsManager = settingsManager
            )
            
            private val openAiRepo = com.omnimap.data.repository.OpenAiRepositoryImpl(
                apiKey = runBlocking { settingsManager.getGeminiApiKey().firstOrNull() } ?: "",
                selectedModel = runBlocking { settingsManager.getSelectedModel().firstOrNull() } ?: "llama3.1",
                baseUrl = runBlocking { settingsManager.getBaseUrl().firstOrNull() },
                settingsManager = settingsManager
            )

            private fun getActiveRepo(): AiInferenceRepository {
                val model = runBlocking { settingsManager.getSelectedModel().firstOrNull() } ?: "gemini-1.5-pro"
                return if (model.startsWith("gemini")) geminiRepo else openAiRepo
            }

            override suspend fun generateNodeSuggestion(contextPrompt: String): Result<String> =
                getActiveRepo().generateNodeSuggestion(contextPrompt)

            override fun isConfigured(): Boolean = getActiveRepo().isConfigured()
            
            override fun getSelectedModel(): String = 
                runBlocking { settingsManager.getSelectedModel().firstOrNull() } ?: "gemini-1.5-pro"

            override suspend fun saveConfiguration(apiKey: String, model: String, baseUrl: String?) {
                settingsManager.saveGeminiApiKey(apiKey)
                settingsManager.saveSelectedModel(model)
                settingsManager.saveBaseUrl(baseUrl)
            }
        }
    }
}