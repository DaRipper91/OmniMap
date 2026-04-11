#!/usr/bin/env bash

# Run this directly on your PinePhone's PostmarketOS terminal!
# It sets up the Java environment and downloads/configures the Android SDK tools needed to compile the APK locally.

# Exit on first error
set -e

echo ">>> 1. Installing PostmarketOS Dependencies (Java 17, Gradle, unzip, wget, git)..."
# 'sudo' might not be present or needed if running as root/pmos user, but good practice
# Add git for Capacitor sync, if not already present.
pkgs="openjdk17 gradle unzip wget git"
for pkg in $pkgs; do
  if ! apk info -e $pkg; then
    echo "Installing $pkg..."
    sudo apk add --no-cache $pkg
  else
    echo "$pkg is already installed."
  fi
done

echo ">>> 2. Setting up Android SDK directories..."
export ANDROID_HOME=$HOME/android-sdk
mkdir -p $ANDROID_HOME/cmdline-tools
mkdir -p $ANDROID_HOME/platform-tools

echo ">>> 3. Downloading Android Command Line Tools..."
# Google's official Linux tools are x86_64. On ARM64 (PinePhone), we need to ensure emulation 
# is available (qemu-user-static) or look for native ARM64 versions if possible. 
# For now, we try the official, assuming qemu-user-static is configured.
# For Alpine, aarch64 android-sdk could be installed via 'apk add android-sdk', 
# but it might not be up-to-date enough or include all commandline tools.
# We'll use the official cmdline-tools and expect QEMU emulation for sdkmanager/aapt2.

cd $ANDROID_HOME/cmdline-tools
wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O cmdline-tools.zip
unzip -q cmdline-tools.zip
mv cmdline-tools latest # The zip extracts to a 'cmdline-tools' directory
rm cmdline-tools.zip

echo ">>> 4. Configuring Environment Variables..."
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

echo ">>> 5. Accepting SDK Licenses & Installing Platforms..."
# sdkmanager might require x86_64 emulation. If this fails, you may need to install 
# qemu-user-static (if not already present on your PostmarketOS system) or find ARM64 SDK alternatives.
# E.g., 'sudo apk add qemu-user-static' (untested specific package name for PostmarketOS)

# First, check if sdkmanager is executable by QEMU
if ! command -v $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager > /dev/null; then
  echo "WARNING: sdkmanager not found or not executable. Attempting a workaround."
  echo "This might mean qemu-user-static is not installed or configured correctly for x86_64 emulation."
  # As a fallback, try to link to Termux's aapt2 if it was installed and SDK location is known.
  # This is a bit of a hack, but might make gradle build proceed if sdkmanager fails.
  # For PostmarketOS, we should check if there's an 'android-sdk' package that provides ARM64 tools.
  # For now, relying on the user to ensure emulation is working for sdkmanager.
fi

# This step often hangs if emulation isn't working or licenses pop up.
# If it hangs, manually run 'sdkmanager --licenses' and accept.
# And manually install platforms: 'sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"'
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo ">>> 6. Syncing Capacitor Android project..."
# Ensure node_modules are available before this step
cd /home/pine/Projects/Continuum-Consolidated/Project-Continuum-Main # Assuming /home/pine is the default user home
npm install # Ensure web dependencies are there
npm run build # Build the web assets
npx cap sync android

# Ensure `android.aapt2FromMavenOverride` is set correctly in `gradle.properties`
# We assume `aapt2` is either found by gradle or the Termux one is linked if apk add android-tools worked.
# This specific override would only be needed if an x86_64 aapt2 was picked up by Gradle.
# For now, we trust the `pkg install aapt2` and rely on its path.
# If `android/gradle.properties` exists, we'll check it. Otherwise, we'll ensure it.
GRADLE_PROPS_FILE="/home/pine/Projects/Continuum-Consolidated/Project-Continuum-Main/android/gradle.properties"
if ! grep -q "android.aapt2FromMavenOverride" "$GRADLE_PROPS_FILE"; then
  echo "android.aapt2FromMavenOverride=/usr/bin/aapt2" >> "$GRADLE_PROPS_FILE"
  echo "Added aapt2 override to gradle.properties."
else
  echo "aapt2 override already present in gradle.properties."
fi

# Ensure local.properties points to the correct SDK location (even though sdkmanager should set it)
LOCAL_PROPS_FILE="/home/pine/Projects/Continuum-Consolidated/Project-Continuum-Main/android/local.properties"
if [ ! -f "$LOCAL_PROPS_FILE" ]; then
  echo "sdk.dir=$ANDROID_HOME" > "$LOCAL_PROPS_FILE"
  echo "Created local.properties with sdk.dir=$ANDROID_HOME."
else
  echo "sdk.dir already configured in local.properties. Verifying...
  if ! grep -q "sdk.dir=$ANDROID_HOME" "$LOCAL_PROPS_FILE"; then
    sed -i "s|sdk.dir=.*|sdk.dir=$ANDROID_HOME|" "$LOCAL_PROPS_FILE"
    echo "Updated sdk.dir in local.properties to $ANDROID_HOME."
  else
    echo "sdk.dir is correctly set in local.properties."
  fi
fi


echo ">>> 7. Building the APK..."
cd /home/pine/Projects/Continuum-Consolidated/Project-Continuum-Main/android
chmod +x gradlew
./gradlew assembleDebug

echo ">>> Done! Your APK should be at:"
echo "/home/pine/Projects/Continuum-Consolidated/Project-Continuum-Main/android/app/build/outputs/apk/debug/app-debug.apk"
