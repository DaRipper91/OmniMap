# Project Roadmap: OmniMap

## ✅ Phase 1: Android Foundation (COMPLETED)
- [x] Project Renamed (OmniMap)
- [x] Initial Domain Layer created (Node, Edge, Database)
- [x] Custom Architect Model initialized
- [x] Task 1: The DAO & Repository Pattern (Kotlin Coroutines & Flow)
- [x] Task 2: The Graph Engine (StateFlow Actor)
- [x] Task 3: Interactive Canvas Prototype (Jetpack Compose)
- [x] Task 4: Haptic Integration (VibratorManager)
- [x] Task 5: Local Inference Bridge (Retrofit/Ollama)

## 🎯 Phase 2: The Native Experience & Intelligence (IN_PROGRESS)
Goal: Transform the foundation into a fully usable, premium native application with integrated local AI and robust data management.

### 📝 Task List
1. **Task 1: The Native UI Shell**
   - Implement Jetpack Compose `Scaffold` with a Bottom Navigation Bar (Dashboard, Graph, Feed).
   - Implement Edge-to-Edge display for immersive, bezel-less UX.
2. **Task 2: Live Intelligence Feed**
   - Build a "Chat/Feed" composable to interact with the `omnimap-architect`.
   - Implement JSON parsing to translate AI responses into graph mutations (auto-creating Nodes/Edges).
3. **Task 3: Global Search & Data Management**
   - Implement Full-Text Search (FTS) in Room for instant Node retrieval.
   - Build native CRUD dialogs (Create/Edit/Delete) for Nodes.
4. **Task 4: Cross-Device Export/Import**
   - Architect a JSON or SQLite backup system to export the mind-map to the Linux workstation and vice-versa.

## 📅 Phase 3: Multi-Platform Expansion (Planned)
- Linux Desktop Package
- Windows Executable
- iOS Compatibility
