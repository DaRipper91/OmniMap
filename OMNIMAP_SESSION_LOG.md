# OmniMap Handover Log: Session 2026-04-12

## 🚀 Project Identity
- **Name:** OmniMap (formerly Continuum)
- **Vision:** Native "Mind-Point" Execution Plane. 
- **Goal:** Professional, tactile, flawless native experience (Gemini/Word-app level).
- **Target Hardware:** Pixel 10 Pro (Primary), Pixel 9 (Dev Rig), Desktop (CachyOS).

## 🛠️ Current Tech Stack (The Pivot)
- **Old (Legacy):** Capacitor / TypeScript / Zustand (To be purged/ported).
- **New (Active):** Kotlin 2.0, Jetpack Compose, Room Database (SQLite), Flow/Coroutines.
- **AI Integration:** Local-first via Ollama (Custom `omnimap-architect` model).

## 📊 Infrastructure Status
- **GitLab:** `git@gitlab.com:DaRipper91/OmniMap.git` (Remote: `gitlab`, Branch: `master`)
- **GitHub:** `https://github.com/DaRipper91/OmniMap.git` (Remote: `github`, Branch: `main`)
- **Local Path:** `/home/daripper/Projects/OmniMap`
- **Scaffold:** `android-native/` directory contains initial Domain Layer (`Node.kt`, `Edge.kt`, `OmniMapDatabase.kt`).

## 🧠 Local AI State
- **Primary Model:** `omnimap-architect:latest` (Custom persona: Senior Android Principal).
- **Tooling:** `aider` configured for local Ollama usage.
- **Core Arsenal:** `qwen2.5-coder:7b`, `marco-o1:7b` (Deep Debug), `deepseek-coder-v2:16b` (Heavyweight).

## 🎯 Next Tasks (Android Foundation)
1. **Task 1:** Implement `NodeDao` and `EdgeDao` in Kotlin for high-speed CRUD.
2. **Task 2:** Port hierarchy/graph traversal logic from TS to Kotlin.
3. **Task 3:** Build the Compose `Canvas` prototype for interactive mind-points.
4. **Task 4:** Integrate `VibratorManager` for tactile haptic feedback.

## ⚙️ Operational Protocol (Non-Negotiable)
- **One Step at a Time:** Do NOT batch tasks. Complete one, verify it, and wait for user steering before starting the next.
- **Explain Before Acting:** For every technical decision (e.g., layout style, library choice), present 2-3 suggestions with their pros/cons. Wait for the user to select or critique the approach before writing code.
- **No Assumptions:** If a path is ambiguous, ask for clarification.

## 📝 Critical Note for Next Agent
The user **hates** "webby" apps. Any suggestion of WebViews or Capacitor is a failure. Focus on **native performance** and **120Hz polish** for the Pixel 10 Pro.
