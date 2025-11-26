# Setup Instructions for Verus Miner

## Before Building

### 1. Download CCMiner Binaries

The app requires ccminer binaries to function. You have several options:

#### Option A: Automated Download (Linux/Mac)
```bash
chmod +x scripts/download_ccminer.sh
./scripts/download_ccminer.sh
```

#### Option B: Manual Download
1. Visit: https://github.com/Oink70/CCminer-ARM-optimized/releases
2. Download the latest release (e.g., ccminer-3.8.3-4)
3. Rename to `ccminer` (no extension)
4. Place in:
   - `app/src/main/assets/ccminer/arm64-v8a/ccminer`
   - Optionally for 32-bit: `app/src/main/assets/ccminer/armeabi-v7a/ccminer`

#### Option C: Use GitHub Actions
The GitHub Actions workflow will automatically download ccminer during build.

### 2. Verify File Structure

```
app/src/main/assets/
└── ccminer/
    ├── arm64-v8a/
    │   └── ccminer          # Required for 64-bit ARM
    └── armeabi-v7a/
        └── ccminer          # Optional for 32-bit ARM
```

### 3. Make Binaries Executable (if on Linux/Mac)
```bash
chmod +x app/src/main/assets/ccminer/arm64-v8a/ccminer
chmod +x app/src/main/assets/ccminer/armeabi-v7a/ccminer
```

## Building the App

### Using Android Studio
1. Open project in Android Studio
2. Wait for Gradle sync
3. Build > Make Project
4. Run > Run 'app'

### Using Command Line
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

## Configuration

### Mining Setup
1. Get a Verus wallet address from:
   - Verus Desktop Wallet
   - Verus Mobile Wallet
   - Exchange (not recommended)

2. Choose a mining pool:
   - Vipor NA: `na.vipor.net:5040` (default)
   - Vipor EU: `eu.vipor.net:5040`
   - Vipor Asia: `asia.vipor.net:5040`
   - Luckpool: `pool.verus.io:9998`

3. Configure in app:
   - Enter your wallet address
   - Select pool
   - Set worker name
   - Choose CPU threads

### Battery Optimization
For best mining performance:
1. Tap battery icon in app
2. Allow app to bypass battery optimization
3. This prevents Android from stopping the mining service

## Testing

1. Enter a valid Verus address
2. Start mining
3. Check notification for mining status
4. Monitor hashrate in app
5. Verify shares on pool website

Expected hashrate:
- Low-end phone: 0.5-2 MH/s
- Mid-range phone: 2-5 MH/s
- High-end phone: 5-10 MH/s

## Troubleshooting

### Mining doesn't start
- Verify ccminer binary exists in assets
- Check logcat for errors: `adb logcat | grep VerusMiner`
- Ensure wallet address is valid

### Low hashrate
- Increase CPU threads in settings
- Close other apps
- Disable battery optimization
- Ensure phone is charging

### App crashes
- Check ccminer binary is correct architecture
- Verify Android version is 7.0+
- Check device has enough free RAM

### Can't connect to pool
- Verify internet connection
- Try different pool server
- Check pool status on pool website
- Firewall/VPN may block stratum connections

## Security Notes

- Never share your private keys
- Only enter your public wallet address
- App does not have internet permissions beyond mining
- Source code is available for audit

## Support

- GitHub Issues: Report bugs and issues
- Verus Discord: https://discord.gg/VRKMP2S
- Pool Support: Contact your mining pool

## Additional Resources

- Verus Coin: https://verus.io/
- Vipor Pool: https://vipor.net/
- CCMiner ARM: https://github.com/Oink70/CCminer-ARM-optimized
- Verus Mining Docs: https://docs.verus.io/economy/start-mining.html
