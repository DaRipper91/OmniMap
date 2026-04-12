# Operational Protocol: OmniMap Development

## ⚙️ Core Rules (Non-Negotiable)

### 1. One Step at a Time
- **Atomic Tasks:** Do NOT batch multiple tasks.
- **Verification:** Complete one task, verify its correctness, and wait for user steering before proceeding.

### 2. Explain Before Acting
- **Suggestion Multiplicity:** For every technical decision, present **2-3 distinct suggestions**.
- **Contextual Rationale:** Each suggestion must include Pros, Cons, and how it affects the Pixel 10 Pro experience.
- **User Gate:** Wait for explicit user selection before writing code.

### 3. No "Webby" Slop
- **Native First:** Any suggestion of WebViews, Capacitor, or Electron-style wrappers is considered a failure.
- **120Hz Target:** Animations and UI logic must be optimized for the Pixel 10 Pro's high refresh rate.

### 4. No Assumptions
- If a requirement is ambiguous, ask for clarification. Do not "Jerry-work" a guess.
