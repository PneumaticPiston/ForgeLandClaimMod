@echo off
title Land Claim Mod - Minecraft Client
color 0b

echo Starting session: %date% %time% > latest.log

echo Building mod...
call ./gradlew clean 2>&1 | tee -a latest.log

echo Building mod...
call ./gradlew build --stacktrace 2>&1 | tee -a latest.log

echo Starting Minecraft client...
call ./gradlew runClient 2>&1 | tee -a latest.log

if errorlevel 1 (
    echo Failed to start Minecraft client | tee -a latest.log
    pause
    exit /b 1
)

echo Session ended: %date% %time% | tee -a latest.log
exit /b 0