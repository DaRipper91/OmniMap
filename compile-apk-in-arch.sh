#!/usr/bin/env bash

# Run this inside your Arch PRoot! (e.g. `proot-distro login archlinux`)
# It sets up the Java environment and downloads the Android SDK tools needed to compile the APK locally.

echo ">>> 1. Installing Arch Dependencies (Java 17, Gradle, unzip, wget)..."
pacman -Syu --noconfirm jdk17-openjdk gradle unzip wget

echo ">>> 2. Setting up Android SDK directories..."
export ANDROID_HOME=$HOME/android-sdk
mkdir -p $ANDROID_HOME/cmdline-tools

echo ">>> 3. Downloading Android Command Line Tools..."
# Note: Google's official Linux tools are x86_64. If you are on a Pixel 10 Pro (aarch64), 
# you will need an emulator like box64 or qemu-x86_64 to run `sdkmanager` and `aapt2`.
# Alternatively, you can pull the aarch64 binaries from the Termux `android-sdk` package.
cd $ANDROID_HOME/cmdline-tools
wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O cmdline-tools.zip
unzip -q cmdline-tools.zip
mv cmdline-tools latest
rm cmdline-tools.zip

echo ">>> 4. Configuring Environment Variables..."
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

echo ">>> 5. Accepting SDK Licenses & Installing Platforms..."
yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo ">>> 6. Building the APK..."
cd /data/data/com.termux/files/home/Projects/Continuum-Consolidated/Project-Continuum-Main/android
chmod +x gradlew
./gradlew assembleDebug

echo ">>> Done! Your APK should be at:"
echo "android/app/build/outputs/apk/debug/app-debug.apk"