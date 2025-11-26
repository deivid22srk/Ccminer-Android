#!/bin/bash

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║           DEBUG SCRIPT - Verus Miner Logs                        ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""

if ! command -v adb &> /dev/null; then
    echo "ERROR: adb not found!"
    echo "Please install Android SDK Platform Tools"
    exit 1
fi

echo "Checking for connected device..."
adb devices

echo ""
echo "Starting logcat monitoring for Verus Miner..."
echo "Press Ctrl+C to stop"
echo ""
echo "═══════════════════════════════════════════════════════════════════"
echo ""

adb logcat -c

adb logcat | grep -E "(MiningViewModel|MiningService|ccminer|VerusMiner)" --color=always
