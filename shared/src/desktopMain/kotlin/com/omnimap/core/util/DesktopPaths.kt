package com.omnimap.core.util

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions

/** Per-user data and config locations, following the XDG spec on Linux. */
object DesktopPaths {
    private val home = System.getProperty("user.home")
    private val os = System.getProperty("os.name").lowercase()

    fun configDir(): File = when {
        os.contains("win") -> File(System.getenv("APPDATA") ?: home, "OmniMap")
        os.contains("mac") -> File(home, "Library/Application Support/OmniMap")
        else -> File(System.getenv("XDG_CONFIG_HOME")?.takeIf { it.isNotBlank() } ?: "$home/.config", "omnimap")
    }

    fun dataDir(): File = when {
        os.contains("win") || os.contains("mac") -> configDir()
        else -> File(System.getenv("XDG_DATA_HOME")?.takeIf { it.isNotBlank() } ?: "$home/.local/share", "omnimap")
    }

    private var instanceLock: java.nio.channels.FileLock? = null

    /**
     * Takes an OS-level lock held for the life of the process, so two OmniMap
     * windows can't each load the graph and overwrite the other's saves.
     * Returns false if another instance already holds it.
     */
    fun acquireInstanceLock(): Boolean {
        val dir = dataDir().apply { mkdirs() }
        val channel = java.io.RandomAccessFile(File(dir, "omnimap.lock"), "rw").channel
        val lock = try {
            channel.tryLock()
        } catch (_: java.nio.channels.OverlappingFileLockException) {
            null
        }
        if (lock == null) {
            channel.close()
            return false
        }
        instanceLock = lock
        return true
    }

    /** chmod 600 where the filesystem supports POSIX permissions; a no-op on Windows. */
    fun restrictToOwner(file: File) {
        try {
            Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("rw-------"))
        } catch (_: UnsupportedOperationException) {
        }
    }

    /** Swap a fully written temp file into place so a crash never leaves a half-written file. */
    fun atomicReplace(tmp: File, target: File) {
        try {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
