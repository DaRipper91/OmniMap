package com.omnimap.data.remote.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AgentCliTest {

    @Test
    fun testIsCliRecognizesCliPrefix() {
        assertTrue(AgentCli.isCli("cli:claude"))
        assertTrue(AgentCli.isCli("cli:copilot:claude-sonnet-5.5"))
        assertTrue(AgentCli.isCli("cli:opencode:opencode/big-pickle"))
        assertFalse(AgentCli.isCli("gemini-2.0-flash"))
        assertFalse(AgentCli.isCli("claude-3-5-sonnet-20241022"))
        assertFalse(AgentCli.isCli("gpt-4o"))
    }

    @Test
    fun testCommandAndSubModelExtraction() {
        assertEquals("claude", AgentCli.command("cli:claude"))
        assertNull(AgentCli.subModel("cli:claude"))

        assertEquals("copilot", AgentCli.command("cli:copilot:claude-sonnet-5.5"))
        assertEquals("claude-sonnet-5.5", AgentCli.subModel("cli:copilot:claude-sonnet-5.5"))

        assertEquals("opencode", AgentCli.command("cli:opencode:opencode/big-pickle"))
        assertEquals("opencode/big-pickle", AgentCli.subModel("cli:opencode:opencode/big-pickle"))
    }

    @Test
    fun testModelIdFormatting() {
        assertEquals("cli:claude", AgentCli.modelId("claude"))
        assertEquals("cli:claude", AgentCli.modelId("claude", "default"))
        assertEquals("cli:claude", AgentCli.modelId("claude", null))
        assertEquals("cli:copilot:claude-sonnet-5.5", AgentCli.modelId("copilot", "claude-sonnet-5.5"))
        assertEquals("cli:opencode:opencode/big-pickle", AgentCli.modelId("opencode", "opencode/big-pickle"))
    }

    @Test
    fun testLabelFormatting() {
        assertEquals("Claude Code", AgentCli.label("cli:claude"))
        assertEquals("GitHub Copilot (claude-sonnet-5.5)", AgentCli.label("cli:copilot:claude-sonnet-5.5"))
        assertEquals("OpenCode (opencode/big-pickle)", AgentCli.label("cli:opencode:opencode/big-pickle"))
    }

    @Test
    fun testLocalOllamaUrlDetection() {
        assertTrue(LocalOllama.isOllama("http://localhost:11434/v1/"))
        assertTrue(LocalOllama.isOllama("http://127.0.0.1:11434/v1/"))
        assertFalse(LocalOllama.isOllama("https://api.openai.com/v1/"))
        assertFalse(LocalOllama.isOllama(null))
    }

    @Test
    fun testRequiresApiKey() {
        // CLI never requires an API key
        assertFalse(requiresApiKey("cli:claude", null))
        assertFalse(requiresApiKey("cli:copilot:claude-sonnet-5.5", null))
        assertFalse(requiresApiKey("cli:opencode:opencode/big-pickle", null))

        // Ollama local endpoint does not require an API key
        assertFalse(requiresApiKey("llama3.1", "http://localhost:11434/v1/"))

        // Remote models require an API key
        assertTrue(requiresApiKey("gemini-2.0-flash", null))
        assertTrue(requiresApiKey("gpt-4o", "https://api.openai.com/v1/"))
        assertTrue(requiresApiKey("claude-3-5-sonnet-20241022", "https://api.anthropic.com/v1/"))
    }
}
