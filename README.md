# 🗺️ OmniMap

**The Native Execution Control Plane for Your Mind.**

OmniMap is a high-performance, local-first, AI-writable graph of thoughts, tasks, and relationships built natively for Android and Desktop. It transforms AI from a chatty assistant into a productive architect of your mental workspace.

## 🧠 Key Features

- **Tactile Graph Canvas:** A physics-based mind-map engine optimized for 120Hz native execution.
- **AI-Architect (Gemini):** Real-time graph mutations powered by Gemini 3.1 Pro. The AI builds your map with you.
- **Local-First Privacy:** Data stays on your device in a robust Room database.
- **Haptic Snap:** Feel your connections with deep hardware integration (Android).
- **Universal Intelligence:** Setup wizard for easy Gemini API configuration without editing code.

## 🚀 Quick Start (Android)

1. **Download the APK** from the latest release.
2. **Launch OmniMap** on your device.
3. **Setup Wizard:** Enter your Google Gemini API key when prompted (Get one for free at [ai.google.dev](https://ai.google.dev)).
4. **Start Flowing:** Tap the canvas to add nodes or use the AI Feed to build complex maps via natural language.

## 🏗️ Development

### Prerequisites
- Android Studio Ladybug or IntelliJ IDEA.
- JDK 17+.

### Building from Source
```bash
# Build Android APK
./gradlew :androidApp:assembleDebug

# Run Desktop App
./gradlew :desktop:run
```

## 🛠️ Tech Stack
- **Language:** Kotlin 2.0 (Multiplatform)
- **UI:** Jetpack Compose / Compose Multiplatform
- **Database:** Room (Android)
- **Intelligence:** Google Gemini 3.1 Pro SDK
- **Architecture:** MVVM + Clean Architecture

---
*Built for tactile perfection and cognitive flow.*
