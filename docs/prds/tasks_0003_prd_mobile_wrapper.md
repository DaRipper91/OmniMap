# Technical Plan: Mobile/Capacitor Wrapper

## Overview
Wrap the Continuum React/Vite project with Capacitor to enable native Android features. Implement a unified filesystem bridge to handle local project vaults and model storage on mobile devices.

## PRD Reference
- File: `docs/prds/0003_prd_mobile_wrapper.md`
- Feature: Mobile/Capacitor Wrapper

## Relevant Files

### Files to Modify
- `package.json` - Add Capacitor dependencies.
- `src/store.ts` - Update persistence logic to support native filesystem.
- `src/App.tsx` - Add platform-specific UI adjustments.

### Files to Create
- `capacitor.config.ts` - Native bridge configuration.
- `src/native-bridge.ts` - Wrapper for Filesystem and HTTP plugins.

---

## Phase 1: Capacitor Initialization

Bootstrap the native project shell.

### Tasks
- [ ] 1.0 **Capacitor Core Setup**
  - [ ] 1.1 Install `@capacitor/core`, `@capacitor/cli`, `@capacitor/android`.
  - [ ] 1.2 Initialize Capacitor with `npx cap init`.
- [ ] 2.0 **Android Platform Integration**
  - [ ] 2.1 Add Android platform via `npx cap add android`.
  - [ ] 2.2 Configure `AndroidManifest.xml` for external storage permissions.

### Verification Criteria
- [ ] `android/` directory is successfully created.
- [ ] `capacitor.config.ts` correctly points to the `dist` directory.

---

## Phase 2: Native Plugin Bridges

Implement the "Singularity Bridge" for native capabilities.

### Tasks
- [ ] 3.0 **Filesystem Bridge**
  - [ ] 3.1 Install `@capacitor/filesystem`.
  - [ ] 3.2 Implement `writeNodeToFile` and `readNodeFromFile` in `src/native-bridge.ts`.
- [ ] 4.0 **Native HTTP Bridge**
  - [ ] 4.1 Install `@capacitor/http`.
  - [ ] 4.2 Update `downloadModel` in `src/store.ts` to use native downloader for large files.

### Verification Criteria
- [ ] UI can trigger a native permission prompt for storage.
- [ ] Small text files can be read/written to the device's Documents folder.

---

## Phase 3: Platform Adaptation

Ensure the UI and Store work seamlessly across Web and Mobile.

### Tasks
- [ ] 5.0 **Unified Storage Provider**
  - [ ] 5.1 Implement logic to detect `Capacitor.getPlatform()`.
  - [ ] 5.2 Redirect store persistence to `Filesystem` on Android.
- [ ] 6.0 **UI Native Polish**
  - [ ] 6.1 Adjust layouts for mobile viewport (safe area insets).
  - [ ] 6.2 Add "Sync to Disk" button for manual vault flushing.

### Verification Criteria
- [ ] App remains functional in the browser (fallback to localStorage).
- [ ] Android build succeeds and runs in emulator/device (manual check).

---

## Phase 4: Review & Audit

### Tasks
- [ ] 7.0 **Agent 7: Technical Review**
  - [ ] 7.1 Verify permission handling logic (denial cases).
  - [ ] 7.2 Audit filesystem paths for Scoped Storage compliance.
- [ ] 8.0 **Agent 8: QA Audit**
  - [ ] 8.1 20-point audit of mobile responsiveness.
  - [ ] 8.2 Final [YES/NO] Report for Part 3.

---

## Risks & Mitigations
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Android Permission rejection | High | High | Implement graceful fallback to internal app storage or read-only mode. |
| Large binary download corruption | Med | High | Verify MD5/SHA256 checksums after native HTTP download. |

## Estimated Effort
- **Complexity:** MEDIUM-HIGH (Requires Android toolchain and native dependency management).
