# OmniMap Project Report: Phase 2 (Native Experience & Intelligence)

## 🎯 Executive Summary
Phase 1 (Foundation) is complete. We are now executing Phase 2, focused on premium native UX and local/cloud AI integration. The UI shell has been successfully modernized with a multi-tab bottom navigation system.

## ✅ Accomplishments (Phase 2)

1. **Task 1: The Native UI Shell**
   - Implemented Jetpack Compose `Scaffold` with a Bottom Navigation Bar.
   - Added tabs for **Dashboard**, **Graph**, and the new **Live Intelligence Feed**.
   - Refactored chat UI into a reusable `ChatContent` component shared between the Graph bottom sheet and the full-screen Feed.
   - Enabled Edge-to-Edge support for immersive UX.

2. **Task 2: Live Intelligence Feed (Pivot to Gemini)**
   - Successfully pivoted the Android AI core from local Ollama to the Google Gemini Pro API.
   - Implemented `GeminiRepositoryImpl` using the official `generativeai` Android SDK.
   - Built a robust JSON parsing engine in `GraphViewModel` that translates AI architectural advice into real-time graph mutations (automatic Node/Edge creation).
   - Integrated system instructions into the AI prompt to ensure structured JSON output from the `omnimap-architect`.

## 🚧 In Progress (Phase 2)

3. **Task 3: Global Search & Data Management**
   - **Objective:** Implement Full-Text Search (FTS) in Room and CRUD dialogs.
   - **Status:** Planning Room FTS integration.

---

# OmniMap Project Report: Phase 1 (Android Foundation) - ARCHIVED
... (rest of the content)

## 🔄 Cross-Device Development Guide (How to Continue)
OmniMap is designed to be built across multiple environments (e.g., Linux Workstation, Android via Termux). To continue development seamlessly on another device, follow this protocol:

### 1. Synchronize the Repository
First, clone or pull the latest changes from the synchronized Git remotes:
```bash
git clone git@gitlab.com:DaRipper91/OmniMap.git
# OR
git clone https://github.com/DaRipper91/OmniMap.git
```

### 2. Unified `.gemini` Configuration Strategy
To maintain the AI's context, memory, and custom agents across your CachyOS desktop and Android Termux environments, you must sync your `.gemini` directory.
- **Symlink or Git-Sync:** Keep a dedicated private repository for your `~/.gemini` folder.
- **On the new device:** Clone your config repo to `~/.gemini` before starting the agent. This ensures the AI instantly knows the `PROJECT_IDENTITY`, the `omnimap-architect` system instructions, and the current operational protocols without having to be re-taught.

### 3. Local Inference Setup (Ollama)
The app relies on local inference. On the new device (or network):
- Ensure Ollama is running and accessible.
- Verify the `omnimap-architect` model is loaded: `ollama run omnimap-architect`.
- Update the API base URL in the app's Retrofit builder if the IP address of the Ollama host changes.

### 4. Build Environment
- **Desktop:** Open the `android-native/` folder in Android Studio Ladybug (or newer) to continue Compose UI development.
- **Android Device (Termux/Shizuku):** Use the pre-existing build scripts (e.g., `compile-apk-in-arch.sh` modified for local build) to compile and install the APK natively on the device via ADB.
