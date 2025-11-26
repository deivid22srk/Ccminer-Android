@echo off
echo ═══════════════════════════════════════════════════════════════════
echo            DEBUG SCRIPT - Verus Miner Logs (Windows)
echo ═══════════════════════════════════════════════════════════════════
echo.

echo Checking for connected device...
adb devices

echo.
echo Starting logcat monitoring for Verus Miner...
echo Press Ctrl+C to stop
echo.
echo ═══════════════════════════════════════════════════════════════════
echo.

adb logcat -c

adb logcat | findstr "MiningViewModel MiningService ccminer VerusMiner"
