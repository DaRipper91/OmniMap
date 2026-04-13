package com.omnimap.core.haptics

interface HapticEngine {
    fun performLightTick()
    fun performHeavySnap()
    fun performErrorBuzz()
}