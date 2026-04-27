plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                implementation("com.google.code.gson:gson:2.11.0")

                // Retrofit (JVM/Android only, but shared here)
                implementation("com.squareup.retrofit2:retrofit:2.11.0")
                implementation("com.squareup.retrofit2:converter-gson:2.11.0")

                // Gemini SDK (Official Multiplatform)
                implementation("dev.shreyaspatil.generativeai:generativeai-google:0.9.0-1.1.0") {
                    exclude(group = "com.google.ai.client.generativeai", module = "generativeai")
                }

                // Common annotations for Room (KMP compatible)
                implementation("androidx.room:room-common:2.6.1")
                // Multiplatform Lifecycle
                implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.0")
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.8.2")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.12.0")
                
                // Room
                val roomVersion = "2.6.1"
                implementation("androidx.room:room-runtime:$roomVersion")
                implementation("androidx.room:room-ktx:$roomVersion")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

dependencies {
    add("kspAndroid", "androidx.room:room-compiler:2.6.1")
}

android {
    namespace = "com.omnimap.shared"
    compileSdk = 34
    buildToolsVersion = "34.0.0"
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
