# Build Instructions for Verus Miner

## ✅ CCMiner Binary Included

The ccminer binary is **already included** in this repository at:
- `app/src/main/assets/ccminer/arm64-v8a/ccminer` (160KB, ARM64)

**No manual download needed!**

## Building the APK

### Prerequisites

- JDK 17
- Android SDK with API 34
- Android Studio (recommended) OR command line tools

### Option 1: Build with Android Studio (Recommended)

1. Open Android Studio
2. Click "Open" and select the `VerusMiner` folder
3. Wait for Gradle sync to complete (may take a few minutes first time)
4. Click Build > Build Bundle(s) / APK(s) > Build APK(s)
5. APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### Option 2: Build with Command Line

```bash
# Navigate to project directory
cd VerusMiner

# Make gradlew executable (Linux/Mac)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

### Option 3: Use GitHub Actions

This repository includes a GitHub Actions workflow that automatically builds the APK:

1. Push code to GitHub
2. Go to Actions tab
3. Download the built APK from artifacts

## Installation

```bash
# Install via ADB
adb install app/build/outputs/apk/debug/app-debug.apk

# Or copy APK to device and install manually
```

## Troubleshooting

### Gradle takes a long time on first build
- This is normal - Gradle downloads dependencies on first run
- Subsequent builds will be faster
- Expect 5-15 minutes for first build

### "SDK location not found"
Create `local.properties` file with:
```
sdk.dir=/path/to/your/Android/Sdk
```

### Build fails with "Kotlin version" error
- Clean build: `./gradlew clean`
- Try again: `./gradlew assembleDebug`

### OutOfMemoryError
Add to `gradle.properties`:
```
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
```

## Release Build (Signed APK)

1. Generate keystore:
```bash
keytool -genkey -v -keystore release.keystore \
  -alias verusminer -keyalg RSA -keysize 2048 -validity 10000
```

2. Add signing config to `app/build.gradle.kts`

3. Build:
```bash
./gradlew assembleRelease
```

## Verifying CCMiner Binary

```bash
# Check binary exists
ls -lh app/src/main/assets/ccminer/arm64-v8a/ccminer

# Check it's ARM64
file app/src/main/assets/ccminer/arm64-v8a/ccminer
# Output should show: ELF 64-bit LSB shared object, ARM aarch64
```

## What's Included

- ✅ Complete Android project
- ✅ Kotlin source code with Jetpack Compose UI
- ✅ CCMiner ARM64 binary (160KB)
- ✅ Material Design 3 components
- ✅ Gradle build files
- ✅ GitHub Actions workflow
- ✅ All dependencies specified in build.gradle.kts

## Requirements

- **Minimum SDK:** Android 7.0 (API 24)
- **Target SDK:** Android 14 (API 34)
- **Architecture:** ARM64 (arm64-v8a) - included binary
- **Build tools:** Gradle 8.2, Kotlin 1.9.20

## Testing

The app can only be tested on **real ARM64 Android devices**:
- Emulators (x86) will not work
- Must have ARM64 processor
- Android 7.0 or higher

```bash
# Install and test
adb install app/build/outputs/apk/debug/app-debug.apk
adb logcat | grep VerusMiner
```

## Size Information

- CCMiner binary: ~160 KB
- Final APK size: ~15-20 MB (with all libraries)
- Installed size: ~25-30 MB

## Notes

- **First build is slow** - be patient!
- **Internet required** during build for dependencies
- **Real device required** for testing (no emulator support)
- **Battery intensive** - mining uses CPU heavily

## Support

If you have issues building:
1. Try in Android Studio first (easier)
2. Check you have JDK 17
3. Check Android SDK is installed
4. Clean and rebuild: `./gradlew clean assembleDebug`
5. Check GitHub Actions logs for reference
