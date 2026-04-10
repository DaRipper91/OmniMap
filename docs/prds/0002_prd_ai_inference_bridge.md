# PRD: AI Inference Bridge (Project Singularity)

## 1. Project Overview
Implement a live bridge between the Continuum frontend and local LLM runtimes (gpt4all, llama.cpp). This replaces the mock suggestion engine with real-time inference using the "Pattern B" (Discovery & Download) strategy.

## 2. Problem Statement
The current system uses static mock data for AI features. To achieve the "Project Singularity" goal, we need an execution control plane where real local models can read the graph state and emit mutations.

## 3. Goals
- **Runtime Discovery:** Auto-detect local LLM servers on localhost or LAN.
- **Model Management:** Implement Pattern B (First-run download catalog + local discovery).
- **Inference Pipeline:** A unified `requestAI` action that handles prompt templating and response parsing.
- **Agent Roles:** Implement "Jules" and other specialized agents as real inference actors.

## 4. Non-Goals
- Hosting a remote API for shared model access.
- Fine-tuning models within the app.

## 5. Functional Requirements

### Must Have (P0)
- [ ] **Discovery Logic:** Health check against `127.0.0.1:4891` (gpt4all) and `localhost:8080` (llama.cpp).
- [ ] **Download Manager:** Capacitor-compatible downloader for `.gguf` files.
- [ ] **Mutation Emitter:** AI ability to return structured JSON mutations that are injected into the Store's pending queue.

### Should Have (P1)
- [ ] **Prompt Templates:** Pre-defined templates for "Summarize Notes" and "Generate Tasks."
- [ ] **Streaming Support:** Visual feedback while the model is thinking.

## 6. Technical Considerations
- **CORS:** Ensure the frontend can communicate with local servers.
- **Storage:** Use `Documents/Continuum/models` for downloaded binaries.

## 7. Success Metrics
- Successfully download a 2GB quantized model on first run.
- Generate a real `SuggestedAction` from a raw note.
