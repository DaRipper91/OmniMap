# NATE_SETUP_MASTER_PROTOCOL.md

## 💻 Unified Development Setup
This protocol establishes a consistent development environment on both your **Anchor** (Arch Laptop) and **Controller** (Pixel 9).

### Tier 8: Core Setup Patterns (7 Patterns)
1. **The Shell:** Default to `fish` on both systems. It provides the high-signal terminal experience required for OmniMap.
2. **The Auth:** Use `gh auth login` on both devices to enable GitHub CLI features.
3. **The AI Key:** Keep your `GOOGLE_API_KEY` in a secure `~/.env` or shell config; never commit it.
4. **The Bridge:** Ensure **Shizuku** is active on the Android Device before running `rish` in Termux.
5. **The Workspace:** Clone OmniMap into `~/Projects/OmniMap` on both devices.
6. **The Sync:** Use a Git-based workflow. Push from one, pull from the other.
7. **The Backup:** Use `termux-setup-storage` to ensure your code is accessible to Android file managers.

### Tier 5: Dev-Level Flags (4 Flags)
- `gemini --version`: Verify your agent is running >= v1.0.0.
- `gh repo view`: Verify connectivity to the OmniMap repository.
- `python3 -m venv venv`: Always use virtual environments for Side-Quest A.
- `npm start`: Standard command for Side-Quest B (Static Web Viewer).

### Tier 4: Scout Weapons (3 Obscure Patterns)
- **Rish-Root:** Use `rish` to execute `adb shell` commands directly from within the Gemini CLI agent.
- **Gemini Context Sync:** Copy `~/.gemini/` contents between devices to maintain "agent memory".
- **Fish Functions:** Define `dagem` aliases in `~/.config/fish/functions/` for one-command AI triggers.

---
**Status:** ✅ Protocol Updated (Tailscale/SSH-Sync Removed)
**Author:** AI Agent Team (architecture-guardian, task-orchestrator, technical-doc-expert)
