# Architectural Design: Native UI Overhaul

## 🧱 Component Restructuring
- **Root Layout:** Move `sidebar` and `main-content` from a horizontal flex to a vertical stack.
- **BottomNav Component:** Create a fixed-bottom component for tab selection.
- **ProjectDrawer Component:** Create a slide-up or slide-in overlay for project selection (replacing the sidebar list).
- **Haptic Bridge:** Wrap `proposeMutation` and `addNote` in a utility that triggers `Haptics.impact({ style: ImpactStyle.Light })`.

## 🎨 Design Tokens (MD3)
- **Primary Surface:** `#1C1B1F` (MD3 Dark).
- **Container Radius:** `28px` (Large Containers).
- **Elevation:** Use tonal overlays instead of drop shadows where possible.

## 🌉 Capacitor Integration
- Add `@capacitor/haptics` to `package.json`.
- Configure `android/app/src/main/res/values/styles.xml` for translucent navigation bars.

---
*Status: Approved by Architect Agent.*
