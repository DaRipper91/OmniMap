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
- **AI routing**: `ModelRoutingAiRepository` sends `gemini*` models to `GeminiRepositoryImpl`, `cli:*` models (desktop only) to `CliAiRepository`, and everything else to `OpenAiRepositoryImpl` (OpenCode Zen, Ollama, OpenAI…). `CliAiRepository` runs an installed agent CLI (Claude Code, Codex, Copilot, Antigravity) in print mode from an empty temp dir, using the CLI's own login. `AiProviders.kt` holds `AgentCli`, `LocalOllama` (model list from `/api/tags`, no key needed) and `requiresApiKey()`; `OpenCodeZen` holds the Zen base URL and fetches the current free models.
- **Linux packaging**: see `packaging/README.md` (tarball + install script, Arch PKGBUILD, CI builds for x86_64 and aarch64).
- **Navigation**: `Screen` sealed class with three routes (`dashboard`, `graph`, `feed`); navigation is handled in `commonMain` using Compose navigation.
- **Adaptive layout**: `OmniMapApp` switches at 840dp window width. Narrow: bottom `NavigationBar`, Architect chat in a bottom sheet. Wide (laptops/desktops): `NavigationRail` without AI Feed, Architect chat in a collapsible 380dp side panel with the draft Commit/Discard card on top (`GraphScreen(isWide = true)`).
- **Canvas coordinates**: `Node.x`/`Node.y` are in **dp**; `CanvasState` (hoisted in `OmniMapApp`) holds zoom/pan in px and each card's measured size, which `EdgeComposable` uses to anchor edges to card borders. Older saves stored px and are converted once on load: desktop `graph.json` version 1 → 2 and Android DB version 3 → 4 divide positions by the display scale.
- **Linking**: drag the handle on a selected node's right edge onto another node (opens `LinkTypeDialog`), or use Links in the selected-node bar (`LinksDialog`) to add, retype or remove links.
- **Settings**: `Screen.Settings` reuses `ProviderPicker` (also used by first-run setup). Google sign-in is hidden while `AuthRepository.isLoginAvailable` is false (both platforms use placeholder OAuth credentials).
- **Canvas input** (`OmniMapCanvas`): drag/pinch on touch; scroll to pan and Ctrl/Cmd+scroll to zoom toward the pointer; keyboard shortcuts once the canvas has focus (N new, L links, E/Enter edit, Delete/Backspace delete, Esc deselect, F or Ctrl/Cmd+0 fit all, Ctrl/Cmd + `=` `-` zoom). In the chat field, Enter sends and Shift+Enter adds a line.
- **DI**: Manual DI via `AppContainer` / `DesktopAppContainer` — no framework (Hilt/Koin) is used.
