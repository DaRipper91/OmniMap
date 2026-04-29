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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
                apiKey = "",
                selectedModel = "gemini-3.1-pro",
                settingsManager = settingsManager
            )
            
            private val openAiRepo = com.omnimap.data.repository.OpenAiRepositoryImpl(
                apiKey = "",
                selectedModel = "llama3.1",
                baseUrl = null,
                settingsManager = settingsManager
            )

            private var currentModel: String = "gemini-3.1-pro"
            private val scope = CoroutineScope(Dispatchers.Main)

            init {
                settingsManager.getSelectedModel()
                    .onEach { currentModel = it }
                    .launchIn(scope)
            }

            private fun getActiveRepo(): AiInferenceRepository {
                return if (currentModel.startsWith("gemini")) geminiRepo else openAiRepo
            }

            override suspend fun generateNodeSuggestion(contextPrompt: String): Result<String> =
                getActiveRepo().generateNodeSuggestion(contextPrompt)

            override suspend fun generateEmbedding(text: String): Result<List<Float>> =
                getActiveRepo().generateEmbedding(text)

            override fun isConfigured(): Boolean = getActiveRepo().isConfigured()
            
            override fun getSelectedModel(): String = currentModel

            override suspend fun saveConfiguration(apiKey: String, model: String, baseUrl: String?) {
                settingsManager.saveGeminiApiKey(apiKey)
                settingsManager.saveSelectedModel(model)
                settingsManager.saveBaseUrl(baseUrl)
            }
        }
    }
}
