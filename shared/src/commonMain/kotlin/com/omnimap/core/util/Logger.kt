package com.omnimap.core.util

interface Logger {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

object OmniLogger : Logger {
    private var logger: Logger? = null

    fun init(platformLogger: Logger) {
        logger = platformLogger
    }

    override fun d(tag: String, message: String) {
        logger?.d(tag, message) ?: println("DEBUG: [$tag] $message")
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        logger?.e(tag, message, throwable) ?: println("ERROR: [$tag] $message ${throwable?.message ?: ""}")
    }
}
