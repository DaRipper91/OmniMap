package com.omnimap.core.settings

import com.omnimap.core.util.DesktopPaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Properties

/**
 * Settings persisted to `$XDG_CONFIG_HOME/omnimap/settings.properties`
 * (`~/.config/omnimap/` by default). The file holds the API key, so it is
 * written owner-read/write only.
 */
class DesktopSettingsManager(
    private val file: File = File(DesktopPaths.configDir(), "settings.properties")
) : SettingsManager {
    private val props = Properties().apply {
        if (file.exists()) file.inputStream().use { load(it) }
    }

    private val _apiKey = MutableStateFlow(props.getProperty(KEY_API_KEY))
    private val _selectedModel = MutableStateFlow(props.getProperty(KEY_MODEL) ?: "gemini-3.1-pro")
    private val _baseUrl = MutableStateFlow(props.getProperty(KEY_BASE_URL))
    private val _firstLaunch = MutableStateFlow(props.getProperty(KEY_FIRST_LAUNCH)?.toBoolean() ?: true)

    /** Current values, readable synchronously so the app can decide its first screen. */
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()
    val baseUrl: StateFlow<String?> = _baseUrl.asStateFlow()

    override fun getGeminiApiKey(): Flow<String?> = apiKey

    override suspend fun saveGeminiApiKey(key: String) {
        _apiKey.value = key
        persist(KEY_API_KEY, key)
    }

    override fun getSelectedModel(): Flow<String> = selectedModel

    override suspend fun saveSelectedModel(model: String) {
        _selectedModel.value = model
        persist(KEY_MODEL, model)
    }

    override fun getBaseUrl(): Flow<String?> = baseUrl

    override suspend fun saveBaseUrl(url: String?) {
        _baseUrl.value = url
        persist(KEY_BASE_URL, url)
    }

    override fun isFirstLaunch(): Flow<Boolean> = _firstLaunch.asStateFlow()

    override suspend fun setFirstLaunchCompleted() {
        _firstLaunch.value = false
        persist(KEY_FIRST_LAUNCH, "false")
    }

    private suspend fun persist(key: String, value: String?) = withContext(Dispatchers.IO) {
        synchronized(props) {
            if (value == null) props.remove(key) else props.setProperty(key, value)
            file.parentFile?.mkdirs()
            val tmp = File(file.parentFile, "${file.name}.tmp")
            tmp.outputStream().use { props.store(it, "OmniMap settings") }
            DesktopPaths.restrictToOwner(tmp)
            DesktopPaths.atomicReplace(tmp, file)
        }
    }

    private companion object {
        const val KEY_API_KEY = "ai.apiKey"
        const val KEY_MODEL = "ai.model"
        const val KEY_BASE_URL = "ai.baseUrl"
        const val KEY_FIRST_LAUNCH = "app.firstLaunch"
    }
}
