plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
                implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.1")
            }
        }
    }
}

val appVersion = "1.0.0"

compose.desktop {
    application {
        mainClass = "com.omnimap.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Rpm
            )
            packageName = "OmniMap"
            packageVersion = appVersion
            description = "Local-first, AI-writable neural graph"
            vendor = "DaRipper91"
            // jlink keeps only the JDK modules listed here (plus what it detects).
            // TLS for OkHttp/Gemini needs jdk.crypto.ec; Gson touches java.sql.
            modules("java.naming", "java.sql", "jdk.crypto.ec", "jdk.unsupported")

            linux {
                packageName = "omnimap"
                iconFile.set(project.file("src/desktopMain/resources/omnimap.png"))
                menuGroup = "Office"
                shortcut = true
                appCategory = "Office"
                rpmLicenseType = "Proprietary"
            }
        }
    }
}

// A self-contained tarball for Arch/CachyOS (and any distro): the jlinked app
// image plus a .desktop entry, icon and install script. Built for the CPU
// architecture of the machine running the build (x86_64 or aarch64).
val linuxArch: String = when (System.getProperty("os.arch")) {
    "amd64", "x86_64" -> "x86_64"
    "aarch64", "arm64" -> "aarch64"
    else -> System.getProperty("os.arch")
}

tasks.register<Tar>("packageLinuxTarball") {
    group = "compose desktop"
    description = "Packages the app image as omnimap-<version>-linux-<arch>.tar.gz"
    dependsOn("createDistributable")
    archiveFileName.set("omnimap-$appVersion-linux-$linuxArch.tar.gz")
    destinationDirectory.set(layout.buildDirectory.dir("compose/binaries/main/tarball"))
    compression = Compression.GZIP
    into("omnimap-$appVersion") {
        from(layout.buildDirectory.dir("compose/binaries/main/app/OmniMap"))
        from(rootProject.file("packaging/linux/omnimap.desktop"))
        from(rootProject.file("packaging/linux/install.sh")) {
            filePermissions { unix("rwxr-xr-x") }
        }
        from("src/desktopMain/resources/omnimap.png")
    }
}
