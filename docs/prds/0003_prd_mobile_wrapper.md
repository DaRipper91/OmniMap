# PRD: Mobile/Capacitor Wrapper (Project Singularity)

## 1. Project Overview
Transition Continuum from a web-only Vite application into a hybrid mobile application using **Capacitor**. This enables native Android capabilities, specifically for handling large `.gguf` models and local filesystem project vaults.

## 2. Problem Statement
The web browser has storage limitations and cannot easily access external storage or perform large-scale HTTP downloads without user intervention. To support local-first project vaults and multi-gigabyte AI models on Android, we need native bridge access.

## 3. Goals
- **Native Wrapper:** Initialize Capacitor and add the Android platform.
- **Filesystem Bridge:** Use `@capacitor/filesystem` to manage the `Documents/Continuum/` project vault.
- **Native Downloads:** Use `@capacitor/http` for high-speed, background-capable model downloads.
- **Platform Detection:** Logic to switch between Web and Native storage providers.

## 4. Functional Requirements

### Must Have (P0)
- [ ] **Capacitor Initialization:** `capacitor.config.ts` configured.
- [ ] **Android Platform:** Successful build of the Android project shell.
- [ ] **Filesystem Permission:** Request and handle `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE`.
- [ ] **Unified FS Interface:** A wrapper that chooses between `localStorage` (Web) and `Filesystem` (Android).

### Should Have (P1)
- [ ] **Background Downloads:** Allow model downloads to continue when the app is backgrounded.
- [ ] **Splash Screen & Icons:** Basic branding for the APK.

## 5. User Stories
As a developer on the go, I want to open my Pixel 9, have the app automatically find my local model in the Downloads folder, and let me capture a note that is saved as a real `.md` file on my phone.

## 6. Technical Considerations
- **Capacitor 6+** should be used for the latest Android support.
- **Scoped Storage:** Ensure compliance with modern Android storage rules (using the Documents directory).

## 7. Success Metrics
- Successful generation of an `android/` directory.
- Verification that a "Node" can be saved as a file on the Android device via the UI.
