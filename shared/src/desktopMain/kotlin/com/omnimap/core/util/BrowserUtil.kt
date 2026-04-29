package com.omnimap.core.util

import java.awt.Desktop
import java.net.URI

object BrowserUtil {
    fun openUrl(url: String) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
        } else {
            val runtime = Runtime.getRuntime()
            val os = System.getProperty("os.name").lowercase()
            when {
                os.contains("win") -> runtime.exec("rundll32 url.dll,FileProtocolHandler $url")
                os.contains("mac") -> runtime.exec("open $url")
                os.contains("nix") || os.contains("nux") -> runtime.exec("xdg-open $url")
            }
        }
    }
}
