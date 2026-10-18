package com.omnimap.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.omnimap.core.connectivity.ConnectivityObserver
import com.omnimap.core.connectivity.NetworkConnectivityObserver
import com.omnimap.core.haptics.AndroidHapticEngine
import com.omnimap.core.haptics.HapticEngine
import com.omnimap.core.settings.AndroidSettingsManager
import com.omnimap.core.settings.SettingsManager
import com.omnimap.data.local.db.OmniMapDatabase
import com.omnimap.data.remote.api.OllamaApi
import com.omnimap.data.repository.GeminiRepositoryImpl
import com.omnimap.data.repository.ModelRoutingAiRepository
import com.omnimap.data.repository.OmniMapRepositoryImpl
import com.omnimap.data.repository.OpenAiRepositoryImpl
import com.omnimap.domain.repository.AiInferenceRepository
import com.omnimap.domain.repository.OmniMapRepository
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
        )
            .addMigrations(pxToDpMigration(context.resources.displayMetrics.density))
            .fallbackToDestructiveMigration()
            .build()
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
        ModelRoutingAiRepository(
            geminiRepo = GeminiRepositoryImpl(
                apiKey = "",
                selectedModel = "gemini-3.1-pro",
                settingsManager = settingsManager
            ),
            openAiRepo = OpenAiRepositoryImpl(
                apiKey = "",
                selectedModel = "llama3.1",
                baseUrl = null,
                settingsManager = settingsManager
            ),
            settingsManager = settingsManager
        )
    }
}

/** Version 3 stored node positions in screen px; version 4 stores dp. Same schema. */
private fun pxToDpMigration(density: Float) = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE nodes SET x = x / ?, y = y / ?", arrayOf<Any>(density, density))
    }
}
