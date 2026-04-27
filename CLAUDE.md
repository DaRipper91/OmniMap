# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build Android debug APK
./gradlew :androidApp:app:assembleDebug

# Run Desktop app (JVM)
./gradlew :desktop:run

# Package Desktop distributable (Deb/Msi/Dmg)
./gradlew :desktop:package

# Build shared module only
./gradlew :shared:compileKotlinAndroid
./gradlew :shared:compileKotlinDesktop

# Compile APK inside Arch Linux container (for cross-compilation)
./compile-apk-in-arch.sh
```

## Project Structure

Three Gradle modules with a Kotlin Multiplatform shared core:

- **`:shared`** — All business logic and UI. Contains three source sets:
  - `commonMain` — Domain models, repository interfaces, all Compose UI screens and ViewModels, AI integration
  - `androidMain` — Room database, DAOs, `OmniMapRepositoryImpl`, `AndroidHapticEngine`, `AppContainer` DI
  - `desktopMain` — `MockOmniMapRepository` (in-memory), `DesktopHapticEngine` (no-op), `DesktopAppContainer` DI

- **`:androidApp`** — Thin shell: `MainActivity` wires `AppContainer` → ViewModels → `OmniMapApp`. `OmniMapApplication` holds the `AppContainer` singleton.

- **`:desktop`** — Thin shell: `main()` in `Main.kt` wires `DesktopAppContainer` → ViewModels → `OmniMapApp` inside a `Window`.

## Architecture

**MVVM + Clean Architecture** with three layers:

1. **Domain** (`commonMain/domain/`) — `Node`, `Edge` entities with `NodeType`/`EdgeType` enums; `OmniMapRepository` and `AiInferenceRepository` interfaces.

2. **Data** (`androidMain/data/`, `desktopMain/data/`, `commonMain/data/`)
   - Android: Room DB (`OmniMapDatabase`) with `NodeDao`/`EdgeDao`; `NodeFts` entity for full-text search
   - Desktop: `MockOmniMapRepository` holds state in `MutableStateFlow` maps
   - Both: `GeminiRepositoryImpl` (in `commonMain`) calls Gemini 1.5 Pro SDK and returns structured JSON

3. **Presentation** (`commonMain/presentation/`) — Three screens sharing a common `OmniMapApp` composable:
   - `GraphScreen` / `GraphViewModel` / `GraphState` / `GraphIntent` — MVI pattern for the interactive canvas; handles node drag, create, edit, delete, edge creation, and AI prompt submission
   - `DashboardScreen` / `DashboardViewModel` — Node search (Room FTS on Android) and JSON export/import
   - `FeedScreen` — Intelligence feed UI

## AI Integration

`GraphViewModel.submitPrompt()` builds a system instruction requiring the AI to respond with a JSON object (`{ nodes, edges, message }`). The response is parsed by `Gson` into `AiMutationResponse`, then applied as live Room mutations.

`GeminiRepositoryImpl` is in `commonMain` and used on both platforms. The Gemini API key must be set in `local.properties`:
```properties
GEMINI_API_KEY=your_key_here
```

In `AppContainer` (Android) and `DesktopAppContainer` (Desktop), the key is currently hardcoded as a placeholder — replace with a `local.properties` read before shipping.

## Platform-Specific Notes

- **Haptics**: `AndroidHapticEngine` uses `VibratorManager` (Android 12+, minSdk 26). `DesktopHapticEngine` is a no-op stub.
- **Persistence**: Android uses Room; Desktop uses `MockOmniMapRepository` (non-persistent, resets on restart). A SQLDelight or JDBC-based desktop implementation is the next persistence milestone.
- **Navigation**: `Screen` sealed class with three routes (`dashboard`, `graph`, `feed`); navigation is handled in `commonMain` using Compose navigation.
- **DI**: Manual DI via `AppContainer` / `DesktopAppContainer` — no framework (Hilt/Koin) is used.
