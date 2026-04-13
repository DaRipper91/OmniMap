<div align="center">

# 🗺️ OmniMap

**The Native Execution Control Plane for Your Mind.**

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Gemini](https://img.shields.io/badge/Gemini-4285F4?style=for-the-badge&logo=google-gemini&logoColor=white)](https://ai.google.dev/)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)

**Stop switching context. Start flowing.**
*OmniMap is a high-performance, local-first, AI-writable graph of thoughts, tasks, and relationships built natively for Android.*

[**Explore the Tech**](#-the-stack) • [**Latest Release**](https://github.com/DaRipper91/OmniMap/releases/tag/v0.2.0-kmp) • [**Build the App**](#-quick-start)

</div>

---

## ⚡ Why OmniMap?

OmniMap isn't just another task manager. It's a **secure, native intelligence engine** that turns your AI from a chatty assistant into a **productive architect** of your mental workspace.

### 🚀 120Hz Tactile Experience
Built with **Jetpack Compose** (Android) and **Compose Multiplatform** (Desktop). Optimized for flagship devices like the **Pixel 10 Pro**. Experience smooth 120Hz animations and sub-millisecond response times.

### 🧠 AI-Writable Graph
Powered by the **Gemini 1.5 Pro SDK**. The AI (**omnimap-architect**) doesn't just suggest; it proposes real-time mutations to your graph. New nodes and edges are created automatically as you think.

### 🛡️ Local-First Data
Your thoughts are yours. All project data is stored locally in a robust **Room** (Android) or **SQLDelight/Mock** (Desktop) database. No cloud required for core functionality.

---

## 💎 Features that Pop

<table width="100%">
  <tr>
    <td width="50%" valign="top">
      <h3>🏗️ Interactive Canvas</h3>
      A physics-based graph engine for visualizing complex project relationships. Drag, drop, and connect ideas with tactile feedback.
    </td>
    <td width="50%" valign="top">
      <h3>💬 Intelligence Feed</h3>
      A dedicated workspace for architectural dialogue with Gemini. Direct the AI to build your project maps via natural language.
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h3>📳 Haptic Snap</h3>
      Deep hardware integration via the Android **VibratorManager**. Feel the connections as they snap into place on the canvas (Android only).
    </td>
    <td width="50%" valign="top">
      <h3>🔍 Global Search</h3>
      Instant retrieval of nodes, tasks, and notes across all project contexts using Full-Text Search.
    </td>
  </tr>
</table>

---

## 👾 The Stack

| Layer | Technology |
| :--- | :--- |
| **Logic** | `Kotlin Multiplatform` + `Coroutines` + `Flow` |
| **UI** | `Compose Multiplatform` (Android + Desktop JVM) |
| **Persistence** | `Room` (Android) / Local Persistence Layer |
| **Intelligence** | `Google Gemini 1.5 Pro SDK` + Local `omnimap-architect` |
| **Hardware** | `VibratorManager` (Native Android Haptics) |

---

## 📥 QUICK START

### 1. Clone the Power
```bash
git clone https://github.com/DaRipper91/OmniMap.git
cd OmniMap
```

### 2. Open in Android Studio / IntelliJ IDEA
Open the root `OmniMap` directory in **Android Studio Ladybug** or **IntelliJ IDEA**.

### 3. Configure AI
Add your Gemini API key to `local.properties` in the root:
```properties
GEMINI_API_KEY=your_api_key_here
```

### 4. Deploy
- **Android:** Select `androidApp` and hit **Run**.
- **Desktop:** Select `desktop` and hit **Run**.

---

<div align="center">

### 🌌 READY TO ELIMINATE CONTEXT SWITCHING?

[**DOWNLOAD THE LATEST APK**](https://github.com/DaRipper91/OmniMap/releases/download/v0.2.0-kmp/app-debug.apk)

---
*Built with 🦾 by DaRipper91 & the 8-Agent Chain*
*Status: **PHASE 2 IN PROGRESS** - Kotlin Multiplatform Transition Complete*

</div>
