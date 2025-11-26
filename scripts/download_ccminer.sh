#!/bin/bash

echo "Downloading ccminer binaries for Android..."

mkdir -p app/src/main/assets/ccminer/arm64-v8a
mkdir -p app/src/main/assets/ccminer/armeabi-v7a

echo "Downloading ARM64 version..."
wget -O app/src/main/assets/ccminer/arm64-v8a/ccminer \
  https://github.com/Oink70/CCminer-ARM-optimized/releases/download/v3.8.3-4/ccminer-3.8.3-4

if [ $? -eq 0 ]; then
    echo "ARM64 version downloaded successfully"
    chmod +x app/src/main/assets/ccminer/arm64-v8a/ccminer
else
    echo "Failed to download ARM64 version"
    echo "Please download manually from:"
    echo "https://github.com/Oink70/CCminer-ARM-optimized/releases"
fi

echo ""
echo "Alternative: You can also compile ccminer yourself"
echo "or use pre-compiled binaries from:"
echo "- https://github.com/Darktron/pre-compiled"
echo "- https://github.com/RAFSuNX/veruscli-termux-miner"
echo ""
echo "Place the ccminer binary in:"
echo "  app/src/main/assets/ccminer/arm64-v8a/ccminer"
echo "  app/src/main/assets/ccminer/armeabi-v7a/ccminer"

ls -lah app/src/main/assets/ccminer/
