# Verus Miner - Android Mining App

Real cryptocurrency mining app for Verus Coin (VRSC) on Android devices using ARM-optimized ccminer.

## Features

- ⛏️ **Real Mining**: Uses genuine ccminer compiled for ARM architecture
- 🎨 **Material Design 3**: Beautiful UI with Material You theming
- 🔋 **Foreground Service**: Keeps mining active with background service
- ⚙️ **CPU Control**: Configure how many CPU cores to use
- 🔌 **Battery Optimization**: Option to disable battery optimization
- 🌐 **Multiple Pools**: Pre-configured with Vipor pool servers (NA, EU, Asia, SA)
- 💼 **Wallet Management**: Input your Verus wallet address
- 📊 **Real-time Stats**: View hashrate, accepted/rejected shares, uptime

## Requirements

- Android 7.0 (API 24) or higher
- ARM64 (arm64-v8a) or ARM (armeabi-v7a) processor
- Internet connection for pool mining

## Installation

### From Source

1. Clone this repository:
```bash
git clone <repository-url>
cd VerusMiner
```

2. Download ccminer binaries:
```bash
./scripts/download_ccminer.sh
```

3. Build with Android Studio or command line:
```bash
./gradlew assembleDebug
```

4. Install the APK on your device:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### From GitHub Actions

Download the latest APK from the Actions tab after each commit.

## Usage

1. Open the app
2. Enter your Verus wallet address
3. Select a mining pool (Vipor NA is default)
4. Configure CPU threads
5. Tap the play button to start mining
6. Grant battery optimization permissions for best performance

## Mining Pools

Pre-configured pools:
- **Vipor NA**: `na.vipor.net:5040` (North America)
- **Vipor EU**: `eu.vipor.net:5040` (Europe)
- **Vipor ASIA**: `asia.vipor.net:5040` (Asia)
- **Vipor SA**: `sa.vipor.net:5040` (South America)
- **Luckpool**: `pool.verus.io:9998` (Global)

## Technical Details

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Mining Engine**: ccminer (ARM-optimized fork by Oink70)
- **Algorithm**: VerusHash 2.0
- **Architecture**: MVVM with Coroutines and Flow

## Project Structure

```
app/
├── src/main/
│   ├── assets/ccminer/        # ccminer binaries
│   ├── java/com/verusminer/app/
│   │   ├── data/              # Data models and preferences
│   │   ├── service/           # Mining foreground service
│   │   ├── ui/theme/          # Material 3 theming
│   │   ├── viewmodel/         # ViewModel layer
│   │   └── MainActivity.kt    # Main UI
│   └── res/                   # Resources
└── build.gradle.kts
```

## Building for Release

1. Generate a signing key:
```bash
keytool -genkey -v -keystore release.keystore -alias verusminer -keyalg RSA -keysize 2048 -validity 10000
```

2. Build release APK:
```bash
./gradlew assembleRelease
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project uses ccminer which is licensed under GPL-3.0.

## Credits

- **ccminer**: [Oink70/CCminer-ARM-optimized](https://github.com/Oink70/CCminer-ARM-optimized)
- **Verus Coin**: [VerusCoin](https://verus.io/)
- **Vipor Pool**: [vipor.net](https://vipor.net/)

## Disclaimer

Mining cryptocurrency on mobile devices can generate heat and consume battery. Use at your own risk. This software is provided "as is" without warranty of any kind.

## Support

For issues related to:
- **App**: Open an issue on GitHub
- **Mining**: Visit [Verus Discord](https://discord.gg/VRKMP2S)
- **Pool**: Contact Vipor support

## Donations

If you find this app useful, consider donating to:

**Verus (VRSC)**: `[Your VRSC Address]`

---

**Happy Mining! ⛏️**
