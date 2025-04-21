@echo off
title Land Claim Mod - Minecraft Server
color 0c

echo Building mod...
call gradlew build

echo Starting Minecraft server...
call gradlew runServer

if errorlevel 1 (
    echo Failed to start Minecraft server
    pause
    exit /b 1
)

exit /b 0