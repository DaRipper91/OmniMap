package com.omnimap.data.repository

import com.omnimap.core.settings.SettingsManager
import com.omnimap.domain.repository.AiInferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeSettingsManager(
    initialModel: String = "gemini-2.0-flash",
    initialKey: String? = "test-key"
) : SettingsManager {
    val modelFlow = MutableStateFlow(initialModel)
    val keyFlow = MutableStateFlow(initialKey)
    val baseUrlFlow = MutableStateFlow<String?>(null)

    override fun getGeminiApiKey(): Flow<String?> = keyFlow
    override suspend fun saveGeminiApiKey(key: String) { keyFlow.value = key }

    override fun getSelectedModel(): Flow<String> = modelFlow
    override suspend fun saveSelectedModel(model: String) { modelFlow.value = model }

    override fun getBaseUrl(): Flow<String?> = baseUrlFlow
    override suspend fun saveBaseUrl(url: String?) { baseUrlFlow.value = url }

    override fun isFirstLaunch(): Flow<Boolean> = MutableStateFlow(false)
    override suspend fun setFirstLaunchCompleted() {}
}

class FakeAiRepository(val tag: String) : AiInferenceRepository {
    var callCount = 0
    override fun isConfigured(): Boolean = true
    override fun getSelectedModel(): String = tag
    override suspend fun saveConfiguration(apiKey: String, model: String, baseUrl: String?) {}
    override suspend fun generateEmbedding(text: String): Result<List<Float>> = Result.success(emptyList())
    override suspend fun generateNodeSuggestion(contextPrompt: String): Result<String> {
        callCount++
        return Result.success("{\"nodes\":[],\"edges\":[],\"message\":\"$tag\"}")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ModelRoutingAiRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRoutesToGeminiWhenModelStartsWithGemini() = runTest {
        val gemini = FakeAiRepository("gemini")
        val openAi = FakeAiRepository("openai")
        val anthropic = FakeAiRepository("anthropic")
        val cli = FakeAiRepository("cli")
        val settings = FakeSettingsManager(initialModel = "gemini-2.0-flash")

        val router = ModelRoutingAiRepository(
            geminiRepo = gemini,
            openAiRepo = openAi,
            anthropicRepo = anthropic,
            cliRepo = cli,
            settingsManager = settings,
            initialModel = "gemini-2.0-flash"
        )

        val res = router.generateNodeSuggestion("test")
        assertTrue(res.isSuccess)
        assertEquals(1, gemini.callCount)
        assertEquals(0, openAi.callCount)
        assertEquals(0, anthropic.callCount)
        assertEquals(0, cli.callCount)
    }

    @Test
    fun testRoutesToAnthropicWhenModelStartsWithClaude() = runTest {
        val gemini = FakeAiRepository("gemini")
        val openAi = FakeAiRepository("openai")
        val anthropic = FakeAiRepository("anthropic")
        val cli = FakeAiRepository("cli")
        val settings = FakeSettingsManager(initialModel = "claude-3-5-sonnet-20241022")

        val router = ModelRoutingAiRepository(
            geminiRepo = gemini,
            openAiRepo = openAi,
            anthropicRepo = anthropic,
            cliRepo = cli,
            settingsManager = settings,
            initialModel = "claude-3-5-sonnet-20241022"
        )

        val res = router.generateNodeSuggestion("test")
        assertTrue(res.isSuccess)
        assertEquals(0, gemini.callCount)
        assertEquals(0, openAi.callCount)
        assertEquals(1, anthropic.callCount)
        assertEquals(0, cli.callCount)
    }

    @Test
    fun testRoutesToCliWhenModelStartsWithCliPrefix() = runTest {
        val gemini = FakeAiRepository("gemini")
        val openAi = FakeAiRepository("openai")
        val anthropic = FakeAiRepository("anthropic")
        val cli = FakeAiRepository("cli")
        val settings = FakeSettingsManager(initialModel = "cli:opencode:opencode/big-pickle")

        val router = ModelRoutingAiRepository(
            geminiRepo = gemini,
            openAiRepo = openAi,
            anthropicRepo = anthropic,
            cliRepo = cli,
            settingsManager = settings,
            initialModel = "cli:opencode:opencode/big-pickle"
        )

        val res = router.generateNodeSuggestion("test")
        assertTrue(res.isSuccess)
        assertEquals(0, gemini.callCount)
        assertEquals(0, openAi.callCount)
        assertEquals(0, anthropic.callCount)
        assertEquals(1, cli.callCount)
    }

    @Test
    fun testRoutesToOpenAiForGenericModels() = runTest {
        val gemini = FakeAiRepository("gemini")
        val openAi = FakeAiRepository("openai")
        val anthropic = FakeAiRepository("anthropic")
        val cli = FakeAiRepository("cli")
        val settings = FakeSettingsManager(initialModel = "gpt-4o")

        val router = ModelRoutingAiRepository(
            geminiRepo = gemini,
            openAiRepo = openAi,
            anthropicRepo = anthropic,
            cliRepo = cli,
            settingsManager = settings,
            initialModel = "gpt-4o"
        )

        val res = router.generateNodeSuggestion("test")
        assertTrue(res.isSuccess)
        assertEquals(0, gemini.callCount)
        assertEquals(1, openAi.callCount)
        assertEquals(0, anthropic.callCount)
        assertEquals(0, cli.callCount)
    }

    @Test
    fun testDynamicSwitchViaSaveConfiguration() = runTest {
        val gemini = FakeAiRepository("gemini")
        val openAi = FakeAiRepository("openai")
        val anthropic = FakeAiRepository("anthropic")
        val cli = FakeAiRepository("cli")
        val settings = FakeSettingsManager(initialModel = "gemini-2.0-flash")

        val router = ModelRoutingAiRepository(
            geminiRepo = gemini,
            openAiRepo = openAi,
            anthropicRepo = anthropic,
            cliRepo = cli,
            settingsManager = settings,
            initialModel = "gemini-2.0-flash"
        )

        // Initially routes to Gemini
        router.generateNodeSuggestion("call 1")
        assertEquals(1, gemini.callCount)

        // Switch to Claude immediately
        router.saveConfiguration("key", "claude-3-7-sonnet", null)
        router.generateNodeSuggestion("call 2")
        assertEquals(1, gemini.callCount)
        assertEquals(1, anthropic.callCount)
    }
}
