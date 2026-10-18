# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build Android debug APK
./gradlew :androidApp:app:assembleDebug

# Run Desktop app (JVM)
./gradlew :desktop:run

# Package Desktop distributable (Deb/Rpm/Msi/Dmg for the host OS)
./gradlew :desktop:package

# Self-contained Linux tarball (Arch/CachyOS/any distro), built for the host CPU
./gradlew :desktop:packageLinuxTarball

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
  - `desktopMain` — `FileOmniMapRepository` (JSON file), `DesktopSettingsManager` (properties file), `DesktopHapticEngine` (no-op), `DesktopAppContainer` DI

- **`:androidApp`** — Thin shell: `MainActivity` wires `AppContainer` → ViewModels → `OmniMapApp`. `OmniMapApplication` holds the `AppContainer` singleton.

- **`:desktop`** — Thin shell: `main()` in `Main.kt` wires `DesktopAppContainer` → ViewModels → `OmniMapApp` inside a `Window`.

## Architecture

**MVVM + Clean Architecture** with three layers:

1. **Domain** (`commonMain/domain/`) — `Node`, `Edge` entities with `NodeType`/`EdgeType` enums; `OmniMapRepository` and `AiInferenceRepository` interfaces.

2. **Data** (`androidMain/data/`, `desktopMain/data/`, `commonMain/data/`)
   - Android: Room DB (`OmniMapDatabase`) with `NodeDao`/`EdgeDao`; `NodeFts` entity for full-text search
   - Desktop: `FileOmniMapRepository` holds state in `MutableStateFlow`s and persists it to a JSON file
   - Both: `ModelRoutingAiRepository` (in `commonMain`) routes to `GeminiRepositoryImpl` (Gemini SDK) or `OpenAiRepositoryImpl` (any OpenAI-compatible API, e.g. OpenCode Zen or Ollama)

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

In practice the key is entered in the Welcome screen's setup step and saved by the platform `SettingsManager` (SharedPreferences on Android, `~/.config/omnimap/settings.properties` on Desktop).

## Platform-Specific Notes

- **Haptics**: `AndroidHapticEngine` uses `VibratorManager` (Android 12+, minSdk 26). `DesktopHapticEngine` is a no-op stub.
- **Persistence**: Android uses Room. Desktop uses `FileOmniMapRepository`, which keeps the graph in memory and writes `$XDG_DATA_HOME/omnimap/graph.json` (debounced; `DesktopAppContainer.shutdown()` flushes on exit). Desktop settings live in `$XDG_CONFIG_HOME/omnimap/settings.properties`.
- **AI routing**: `ModelRoutingAiRepository` sends `gemini*` models to `GeminiRepositoryImpl` and everything else to `OpenAiRepositoryImpl` (OpenCode Zen, Ollama, OpenAI…). `OpenCodeZen` holds the Zen base URL and fetches the current free models.
- **Linux packaging**: see `packaging/README.md` (tarball + install script, Arch PKGBUILD, CI builds for x86_64 and aarch64).
- **Navigation**: `Screen` sealed class with three routes (`dashboard`, `graph`, `feed`); navigation is handled in `commonMain` using Compose navigation.
- **Adaptive layout**: `OmniMapApp` switches at 840dp window width. Narrow: bottom `NavigationBar`, Architect chat in a bottom sheet. Wide (laptops/desktops): `NavigationRail`, Architect chat in a collapsible 380dp side panel (`GraphScreen(isWide = true)`).
- **Canvas input** (`OmniMapCanvas`): drag/pinch on touch; scroll to pan and Ctrl/Cmd+scroll to zoom toward the pointer; keyboard shortcuts once the canvas has focus (N new, E/Enter edit, Delete/Backspace delete, Esc deselect, Ctrl/Cmd + `=` `-` `0` zoom). In the chat field, Enter sends and Shift+Enter adds a line.
- **DI**: Manual DI via `AppContainer` / `DesktopAppContainer` — no framework (Hilt/Koin) is used.
