package com.omnimap.di

import android.content.Context
import androidx.room.Room
import com.omnimap.core.connectivity.ConnectivityObserver
import com.omnimap.core.connectivity.NetworkConnectivityObserver
import com.omnimap.core.haptics.AndroidHapticEngine
import com.omnimap.core.haptics.HapticEngine
import com.omnimap.data.local.db.OmniMapDatabase
import com.omnimap.data.remote.api.OllamaApi
import com.omnimap.data.repository.GeminiRepositoryImpl
import com.omnimap.data.repository.OmniMapRepositoryImpl
import com.omnimap.domain.repository.AiInferenceRepository
import com.omnimap.domain.repository.OmniMapRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(private val context: Context) {
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

    val aiInferenceRepository: AiInferenceRepository by lazy {
        // Pivot: Switching from local Ollama to Google Gemini API
        // NOTE: The user should provide the actual API key via environment variable or local.properties
        val apiKey = "REPLACE_WITH_GEMINI_API_KEY" 
        GeminiRepositoryImpl(apiKey = apiKey)
    }
}