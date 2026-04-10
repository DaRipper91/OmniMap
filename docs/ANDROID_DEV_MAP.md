# Android APK Dev Plan Map (Task 0.0)

## 1. Capacitor Plugins
To enable local-first AI and project management on Android, we will utilize the following Capacitor plugins:
- **@capacitor/filesystem:** Mandatory for reading and writing Markdown project files, tasks, and notes. It allows access to external storage to prevent model deletion during app updates.
- **@capacitor/http:** Essential for downloading and managing AI model manifest files and `.gguf` binaries. This provides a more robust networking layer for large file transfers.
- **@capacitor/device:** Required for runtime hardware identification (e.g., checking available RAM for model selection).
- **@capacitor/app:** For managing app lifecycle and deep linking.

## 2. Android Permissions
The following permissions must be declared in `AndroidManifest.xml`:
- **READ_EXTERNAL_STORAGE / WRITE_EXTERNAL_STORAGE:** Necessary for accessing the project vault and the `models/` directory on the device's storage.
- **INTERNET:** Required for downloading models and manifest updates.
- **ACCESS_NETWORK_STATE:** To prevent large model downloads on metered connections.
- **FOREGROUND_SERVICE:** Optional, but potentially required for background model scanning or long-running synchronization tasks.

## 3. Storage Paths (Pixel 9 / Android Standard)
To ensure data persistence and ease of discovery, the following storage structure is proposed:

### Project Vault (Markdown)
- **Primary:** `Documents/Continuum/`
- **Structure:**
  - `/projects/`: Contains individual project folders and `.md` files.
  - `/ideal-notes/`: Unstructured frictionless capture.
  - `/archive/`: Completed or snoozed content.

### AI Models (.gguf)
- **Primary:** `Download/Continuum/models/` (Standard for user-managed files).
- **Secondary (Hidden):** `Android/data/com.continuum.app/files/models/` (App-private storage if needed).
- **Format:** `[model-name]-[quantization].gguf`
- **Manifest:** `models.json` (Local registry of discovered and downloaded models).

---
*Status: Task 0.0 Completed. Approved by the Architect Agent.*