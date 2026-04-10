# Continuum Agent Definitions

## The Continuum 8-Agent Chain
All non-trivial implementation tasks must follow this chain to enforce discipline, minimize hallucination, and prevent scope creep.

1. **Product Definition Agent:** Defines exact product scope and UI requirements (PRD).
2. **Architect Agent:** Translates PRD into technical architecture, data flow, and risk mitigation.
3. **Planner Agent:** Creates explicit step-by-step implementation plans (`PLAN.md`).
4. **Scaffolding Agent:** High-speed execution agent for generating project shell and folder structure.
5. **Coding Agent:** The workhorse that writes the actual logic.
6. **Review Agent:** Audits the code against the PRD, scope rules, and undo-history compliance.
7. **QA & Compliance Agent:** Audits UI for WCAG AA compliance and performance.
8. **Document Agent:** Updates `PROJECT_REPORT.md` and generates Master Handbooks.

## External Agents
- **Jules (Google Auto Coding Bot):** Treated as an external execution agent. Can prepare PRs and report status but cannot directly mutate Continuum state without explicit authorization.

## Agent Permissions
- **Read:** All agents may read across all projects.
- **Write:** Agents may only write within the *active project*.
- **Approval:** Any writes outside the active project require explicit user approval.