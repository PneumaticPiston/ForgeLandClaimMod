@echo off
title Land Claim Mod - Minecraft Client
color 0b

echo Building mod...
call gradlew build

echo Starting Minecraft client...
call gradlew runClient

if errorlevel 1 (
    echo Failed to start Minecraft client
    pause
    exit /b 1
)

exit /b 0