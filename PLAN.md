# CONTINUUM - Development Plan & Task Mapping

## Overview
Based on the `Edited-CoPilot.txt` document and recent user steering, this is the authoritative task list and development map for building the Continuum Android APK (and broader ecosystem) step-by-step.

## Scope Definition (CRITICAL)
### In Scope
- Bootstrap the Continuum project using the strict 8-Agent Chain.
- Execute Phase 0 through Phase 6 as defined in the master plan.
- **NEW (Task 0.0):** Android APK Dev Plan Map (Capacitor plugins, permissions, model storage).
- Maintain `PROJECT_REPORT.md` discipline after every single task.

### Out of Scope
- Cloud synchronization (Local-first strictly enforced).
- UI Polish in early phases (Ugly but functional is fine for v0.1).

## Agent Chain Enforcement
For every development task, the following chain **must** be respected:
1. **Architect Agent:** Confirms scope and intent.
2. **Planner Agent:** Breaks work into explicit steps.
3. **Coding / Scaffolding Agent:** Writes files and folders.
4. **Review Agent:** Verifies correctness and rule compliance.
5. **Documentation Agent:** Updates `PROJECT_REPORT.md` and docs.

---

## 🗺️ Implementation Phases & Task Mapping

### Phase 0: Project Initialization & Discipline
- [x] **Task 0.0: Android APK Dev Plan Map**
  - Define Capacitor plugins (Filesystem, HTTP for model downloading).
  - Define Android Permissions (Read/Write external storage).
  - Define Storage Paths for Markdown files vs. `.gguf` models.
- [x] **Task 0.1:** Initialize repository & project structure (`/projects/continuum/`).
- [x] **Task 0.2:** Create `PROJECT_REPORT.md` with initial context.
- [x] **Task 0.3:** Set up agent chain enforcement rules in `docs/rules.md`.

### Phase 1: Core Domain & State (No UI Yet)
- [x] **Task 1.1:** Define core data models (Project, Task, Ideal Note, Suggested Action).
- [x] **Task 1.2:** Implement Zustand core store (Unified store, serializable state).
- [x] **Task 1.3:** Implement mutation pipeline (Hooks for approval).
- [x] **Task 1.4:** Implement temporary undo history (Pre-mutation snapshots, TTL-based cleanup).

### Phase 2: Project Scope & Safety Rules
- [x] **Task 2.1: Active project context system.**
- [x] **Task 2.2: Scope validation logic (Detect off-project requests).**
- [x] **Task 2.3: Cross-project write approval flow.**


### Phase 3: Suggested Actions Engine
- [x] **Task 3.1: Suggested Action lifecycle (Active / Snoozed / Archived / Accepted).**
- [x] **Task 3.2: Context-based resurfacing logic.**

- [x] **Task 3.3: Conversion flows (Suggested -> Task/Note/Ignore).**

### Phase 4: AI Runtime & Model Management
- [x] **Task 4.1: Model registry & manifest format.**
- [x] **Task 4.2: Filesystem scanning (manual + auto for storage).**

- [x] **Task 4.3: Background scan scheduling.**
- [x] **Task 4.4: Runtime health check & model selection.**

### Phase 5: Agents, Skills & External Systems
- [x] **Task 5.1: Agent & skill definitions.**
- [x] **Task 5.2: Agent permission model.**

- [x] **Task 5.3: External agent integration framework (read + request).**

### Phase 6: Minimal Functional UI
- [x] **Task 6.1: Project list & active project selector.**
- [x] **Task 6.2: Task list view.**
- [x] **Task 6.3: Thought capture input (Notes).**
- [x] **Task 6.4: Suggested actions panel.**
- [x] **Task 6.5: Undo notification surface.**

### Phase 7: Native Mobile UI Overhaul (Material Design 3)
- [x] **Task 7.1:** Install `@capacitor/haptics` and configure dependencies.
- [x] **Task 7.2:** Implement `BottomNavigationBar` and refactor `App.tsx` layout.
- [x] **Task 7.3:** Implement `ProjectDrawer` (Slide-up selection).
- [x] **Task 7.4:** Apply MD3 Styling (28px radius, tonal surfaces, typography).
- [x] **Task 7.5:** Integrate Haptics into mutation and capture flows.
- [x] **Task 7.6:** Implement spring transitions via `framer-motion`.

### Phase 8: Persistence & Stability
- [x] **Task 8.1:** Harden AI Parser and Fix Interval Leak.
- [x] **Task 8.2:** Implement LocalStorage fallback.
- [x] **Task 8.3:** Implement Capacitor Filesystem persistence.
- [x] **Task 8.4:** Create Auto-Save debouncer.

### Phase 9: Live Intelligence Integration
- [x] **Task 9.1:** Define standard AI Agent Personas (Architect, Researcher, Coder).
- [x] **Task 9.2:** Implement `initializeRuntimes` with GPT4All discovery.
- [x] **Task 9.3:** Add `isThinking` state and UI feedback (Spinners/Pulse).
- [x] **Task 9.4:** Connect `requestAI` to live GPT4All endpoint.
- [x] **Task 9.5:** Implement Error/Offline recovery for AI requests.

### Phase 10: The Master Synchronization Hub (Nexium Bridge)
- [ ] **Task 10.1: Nexium Portal Monitor**
  - Implement UI to list active cloud sessions (`jules remote list`).
- [ ] **Task 10.2: Briefing Generator UI**
  - Add "Open Portal" button to package local project state.
- [ ] **Task 10.3: Result Ingestion**
  - Trigger `jules remote pull --apply` from within the app.
- [ ] **Task 10.4: Graph Persistence Hardening**
  - Ensure complex graph nodes are properly restored after handoff.

---

## 🚦 Execution Directive
We will execute **one task at a time**, respecting the designated phase order. No jumping ahead. After each task, we update the `PROJECT_REPORT.md`.