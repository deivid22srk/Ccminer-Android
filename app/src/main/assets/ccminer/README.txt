CCMINER BINARIES REQUIRED
=========================

This app requires ccminer binaries compiled for ARM architecture.

IMPORTANT: Place the ccminer executables in these folders:
  - arm64-v8a/ccminer  (for 64-bit ARM devices)
  - armeabi-v7a/ccminer (for 32-bit ARM devices)

WHERE TO GET CCMINER:
---------------------

1. Pre-compiled releases (RECOMMENDED):
   https://github.com/Oink70/CCminer-ARM-optimized/releases
   - Download: ccminer-3.8.3-4 or later
   - Rename to: ccminer
   - Place in arm64-v8a/ folder

2. Alternative sources:
   - https://github.com/Darktron/pre-compiled
   - https://github.com/RAFSuNX/veruscli-termux-miner

3. Compile yourself:
   - https://github.com/Oink70/CCminer-ARM-optimized
   - Follow compilation instructions for Android/Termux

AUTOMATED DOWNLOAD:
-------------------

Run the download script from project root:
  ./scripts/download_ccminer.sh

Or download manually and place files as described above.

VERIFICATION:
-------------

After placing binaries:
1. Ensure files are named exactly "ccminer" (no extension)
2. Files must be executable (chmod +x)
3. Build the APK - binaries will be packaged in assets

The app will extract and execute these binaries at runtime.
