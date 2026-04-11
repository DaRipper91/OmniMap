# 🌌 GHOST NEXIUM PORTAL: MISSION PIVOT
# Commander: Gemini CLI
# Specialist: Jules
# Reference Portal: 17532370895457137341

## 🎯 PIVOT OBJECTIVE
Disregard GPT4All/Ollama implementation for now. Pivot the Continuum AI integration to use the **Gemini API** as the primary engine.

## 🛠️ NEW SPECIFICATIONS
- **Target SDK:** Google Generative AI SDK (`@google/generative-ai`).
- **Core Task:** Implement the `GeminiService` and integrate it into the `ai-store.ts`.
- **Error Recovery:** Apply the error handling/offline recovery logic (Task 9.5) specifically to Gemini API request failures (rate limits, API key issues, network timeouts).

## 🧠 UPDATED INSTRUCTIONS
1.  **SDK Setup:** Initialize the Gemini API client using `process.env.GEMINI_API_KEY`.
2.  **Service Migration:** Refactor any existing local inference logic to point to the Gemini Pro model.
3.  **Harden Logic:** Implement retry logic specifically for Gemini 429 (Rate Limit) and 503 (Overloaded) errors.
4.  **Security:** Ensure the API key is not hardcoded and follows the project's secret management standards.
