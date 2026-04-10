# Continuum Execution Rules

## Global Enforcement Rules
- **Explicit Chains:** You MUST operate using explicit agent chains for any non-trivial task. Single-agent execution is forbidden for implementation.
- **Verification Gates:** Each step in the chain must be verified by the next agent. (e.g., Review Agent must verify Coding Agent's work).
- **Step-By-Step:** You MUST work step by step. Do NOT jump ahead or batch major steps together.
- **No Ambiguity:** If any instruction or requested scope is ambiguous, you MUST stop and ask for clarification. Never guess intent.

## The Continuum 8-Agent Chain (Implementation Standard)
1. **Product Definition Agent:** Defines exact product scope and UI requirements (PRD).
2. **Architect Agent:** Translates PRD into technical architecture, data flow, and risk mitigation.
3. **Planner Agent:** Creates explicit step-by-step implementation plans (`PLAN.md`).
4. **Scaffolding Agent:** High-speed execution agent for generating project shell and folder structure.
5. **Coding Agent:** The workhorse that writes the actual logic.
6. **Review Agent:** Audits the code against the PRD, scope rules, and undo-history compliance.
7. **QA & Compliance Agent:** Audits UI for WCAG AA compliance and performance.
8. **Document Agent:** Updates `PROJECT_REPORT.md` and generates Master Handbooks.

## Project Execution Discipline
- **Active Project:** You are working on a SINGLE active project (Continuum) unless told otherwise.
- **Persistent Memory:** `PROJECT_REPORT.md` is the source of truth. It MUST be updated after EVERY completed step.
- **Plan Updates:** `PLAN.md` must be updated to track task completion status.

## Scope & Cross-Project Operations
- **Scope Containment:** Agents may ONLY write within the active project.
- **Approval Flow:** Any write outside the active project REQUIRES explicit user approval.
- **Undo History:** The system maintains a temporary undo history for AI-initiated changes to allow safe regression.

## AI Interactions
- AI agents act as **writers**, emitting state mutations.
- The system queues these mutations for user approval depending on the agent's permission policy.
- Suggested Actions maintain a temporal state (`active`, `snoozed`, `archived`, `accepted`).