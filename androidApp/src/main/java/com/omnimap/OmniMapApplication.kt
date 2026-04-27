package com.omnimap

import android.app.Application
import com.omnimap.core.util.AndroidLogger
import com.omnimap.core.util.OmniLogger
import com.omnimap.di.AppContainer

class OmniMapApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        OmniLogger.init(AndroidLogger())
        container = AppContainer(this)
    }
}
