# Build Notes

## Important: CCMiner Binary Required

This app requires ccminer binaries to function. The GitHub Actions workflow will attempt to download them automatically, but for local builds:

### Quick Setup
```bash
# Run the download script
./scripts/download_ccminer.sh
```

### Manual Setup
1. Download ccminer from: https://github.com/Oink70/CCminer-ARM-optimized/releases
2. Place in: `app/src/main/assets/ccminer/arm64-v8a/ccminer`
3. Make executable: `chmod +x app/src/main/assets/ccminer/arm64-v8a/ccminer`

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on device
./gradlew installDebug

# Clean build
./gradlew clean assembleDebug
```

## APK Output

Built APKs will be located at:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## GitHub Actions

The included workflow (`.github/workflows/build.yml`) will:
1. Download ccminer automatically
2. Build the APK
3. Upload as artifact

You can download built APKs from the Actions tab on GitHub.

## Release Signing

For production releases, you need to:

1. Generate signing key:
```bash
keytool -genkey -v -keystore release.keystore \
  -alias verusminer -keyalg RSA -keysize 2048 -validity 10000
```

2. Add to `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../release.keystore")
            storePassword = "your-password"
            keyAlias = "verusminer"
            keyPassword = "your-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

3. Build signed APK:
```bash
./gradlew assembleRelease
```

## Minimum Requirements

- JDK 17
- Android SDK 34
- Gradle 8.2+
- 4GB RAM for building

## Testing

Test on real Android device (ARM architecture required):
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
adb logcat | grep VerusMiner
```

## Known Issues

- Emulators won't work (x86 architecture, no ccminer support)
- Requires ARMv8 (64-bit) or ARMv7 (32-bit) device
- Some devices may have thermal throttling
- Battery optimization must be disabled for continuous mining

## Support

See SETUP_INSTRUCTIONS.md for detailed configuration and troubleshooting.
