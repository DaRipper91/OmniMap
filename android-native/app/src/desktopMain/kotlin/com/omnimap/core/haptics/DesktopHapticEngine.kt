package com.omnimap.core.haptics

class DesktopHapticEngine : HapticEngine {
    override fun performLightTick() {
        // No-op on desktop
    }

    override fun performHeavySnap() {
        // No-op on desktop
    }

    override fun performErrorBuzz() {
        // No-op on desktop
    }
}