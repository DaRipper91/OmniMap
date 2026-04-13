# OmniMap Execution Rules

## Global Enforcement Rules
- **Explicit Chains:** You MUST operate using explicit agent chains for any non-trivial task. Single-agent execution is forbidden for implementation.
- **Verification Gates:** Each step in the chain must be verified by the next agent. (e.g., Review Agent must verify Coding Agent's work).
- **Step-By-Step:** You MUST work step by step. Do NOT jump ahead or batch major steps together.
- **No Ambiguity:** If any instruction or requested scope is ambiguous, you MUST stop and ask for clarification. Never guess intent.

## The OmniMap 8-Agent Chain (Implementation Standard)
1. **Product Definition Agent:** Defines exact product scope and UI requirements (PRD).
2. **Architect Agent:** Translates PRD into technical architecture, data flow, and risk mitigation.
3. **Planner Agent:** Creates explicit step-by-step implementation plans (`PLAN.md`).
4. **Scaffolding Agent:** High-speed execution agent for generating project shell and folder structure.
5. **Coding Agent:** The workhorse that writes the actual logic.
6. **Review Agent:** Audits the code against the PRD, architecture, and performance targets.
7. **QA & Compliance Agent:** Audits UI for accessibility and 120Hz performance.
8. **Document Agent:** Updates `PROJECT_REPORT.md` and generates Master Handbooks.

## Project Execution Discipline
- **Active Project:** You are working on a SINGLE active project (OmniMap) unless told otherwise.
- **Persistent Memory:** `PROJECT_REPORT.md` is the source of truth. It MUST be updated after EVERY completed step.
- **Plan Updates:** `PROJECT_ROADMAP.md` must be updated to track task completion status.

## Scope & Cross-Project Operations
- **Scope Containment:** Agents may ONLY write within the active project (`android-native/`).
- **Hardware Target:** All code must be optimized for the Pixel 10 Pro hardware features (Haptics, Refresh Rate).

## AI Interactions
- AI agents act as **architects**, emitting structured JSON state mutations.
- The system parses these JSON responses in `GraphViewModel` to automatically evolve the graph.
- All AI-initiated changes are stored in Room and reflected in the UI via Kotlin Flow.