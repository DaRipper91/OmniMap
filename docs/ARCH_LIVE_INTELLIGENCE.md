# Architectural Design: Live Intelligence

## 🧱 Logic Components
- **Runtime Discovery:** On app boot, ping the `gpt4all` server. If available, register it as the active `online` runtime.
- **Request Lifecycle:**
  1. UI triggers `requestAI`.
  2. Store sets `isThinking: true`.
  3. `ai-bridge` sends the request to the local server.
  4. Response is parsed and used to `proposeMutation`.
  5. Store sets `isThinking: false`.
- **System Prompts:** Standardize the "Architect" persona in the AI bridge to ensure JSON payloads are always formatted correctly for the `proposeMutation` pipeline.

## 🌉 Store Extensions
- Add `isThinking: boolean` to `ContinuumState`.
- Add `initializeRuntimes` action to check connectivity on boot.

---
*Status: Approved by Architect Agent.*
