package com.omnimap

import android.app.Application
import com.omnimap.di.AppContainer

class OmniMapApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
