# Changelog

## Version 1.0.0 - Initial Release

### ✅ Features Implemented

#### Core Functionality
- **Real Mining**: Verus Coin mining using CCMiner ARM64 binary (not simulated)
- **Material Design 3**: Beautiful UI with Jetpack Compose
- **Foreground Service**: Keeps mining active in background
- **Battery Management**: Option to disable battery optimization
- **CPU Control**: Configure number of CPU threads (1 to max)

#### Configuration
- **Wallet Address**: Enter your Verus wallet address
- **Mining Pool**: Pre-configured with 5 pools:
  - Vipor NA (North America) - default
  - Vipor EU (Europe)
  - Vipor ASIA (Asia)
  - Vipor SA (South America)
  - Luckpool (Global)
- **Worker Name**: Customize your miner identification
- **Threads**: Slider to select CPU cores to use
- **Persistent Settings**: Auto-save all configurations

#### Statistics
- **Real-time Hashrate**: H/s, KH/s, MH/s display
- **Accepted Shares**: Track successful submissions
- **Rejected Shares**: Monitor failed submissions
- **Uptime**: Mining duration (HH:MM:SS)
- **Visual Status**: Animated status indicator

#### User Interface
- **Blue gradient theme** matching Verus branding
- **Elevated cards** with shadows
- **Smooth animations** on state changes
- **Floating Action Button** for start/stop
- **Material Icons Extended** throughout
- **Responsive layout** for all screen sizes

### 📦 What's Included

- **CCMiner Binary**: ARM64 binary (160KB) included in assets
- **Complete Source Code**: Kotlin with Jetpack Compose
- **MVVM Architecture**: Clean separation of concerns
- **Coroutines & Flow**: Modern async programming
- **DataStore**: Persistent configuration storage
- **GitHub Actions**: Automated APK build workflow
- **Documentation**: Complete guides in English and Portuguese

### 🔧 Technical Details

- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Architecture**: ARM64 (arm64-v8a)
- **Language**: Kotlin 1.9.20
- **UI Framework**: Jetpack Compose
- **Material**: Material Design 3 (1.2.0)
- **Build Tool**: Gradle 8.2

### 📝 Known Limitations

- **ARM64 Only**: Only works on ARM64 devices (most modern phones)
- **No Emulator Support**: Cannot run on x86 emulators
- **Battery Intensive**: Mining consumes significant battery
- **Heat Generation**: Extended mining may cause device heating
- **No Pool Switching**: Cannot change pool while mining

### 🔄 Future Improvements (Planned)

- [ ] Auto-restart on crash
- [ ] Temperature monitoring
- [ ] Pool auto-switching on failure
- [ ] Mining scheduler (time-based)
- [ ] Multiple wallet support
- [ ] Statistics history
- [ ] Dark/Light theme toggle
- [ ] Export mining logs

### 🐛 Bug Fixes

- Fixed Gradle wrapper corruption issue
- Removed plugin.compose dependency
- Included ccminer binary in repository
- Fixed GitHub Actions workflow

### 📚 Documentation Added

- `README.md` - Project overview (English)
- `LEIA-ME.txt` - User guide (Portuguese)
- `BUILD_INSTRUCTIONS.md` - Build guide (English)
- `LEIA-ME-CONSTRUCAO.txt` - Build guide (Portuguese)
- `PROJECT_SUMMARY.md` - Complete feature summary
- `SETUP_INSTRUCTIONS.md` - Detailed setup guide
- `BUILD_NOTES.md` - Developer notes
- `CHANGELOG.md` - This file

### 🎯 Requirements

- Android 7.0+ device with ARM64 processor
- Internet connection for pool mining
- Sufficient battery or charger recommended
- Verus wallet address (get from Verus wallet app)

### ⚠️ Important Notes

- **Real Mining**: This app performs actual cryptocurrency mining
- **Not Simulated**: Uses genuine ccminer binary
- **Battery Usage**: Mining is CPU-intensive
- **No Guarantees**: Mining profitability depends on device and network
- **User Responsibility**: User is responsible for battery and heat management

### 📄 License

- **App Code**: MIT License
- **CCMiner**: GPL-3.0 License

### 🙏 Credits

- **CCMiner ARM**: Oink70/CCminer-ARM-optimized
- **Pre-compiled Binary**: Darktron/pre-compiled
- **Verus Coin**: VerusCoin project
- **Vipor Pool**: vipor.net

---

**Release Date**: November 26, 2025
**Build**: Debug v1.0.0
**Package**: com.verusminer.app
