@echo off
title Land Claim Mod Launcher
color 0a

:menu
cls
echo Land Claim Mod Launcher
echo =====================
echo 1. Start Minecraft Client
echo 2. Start Minecraft Server
echo 3. Start Both (Client and Server)
echo 4. Exit
echo.
set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" goto client
if "%choice%"=="2" goto server
if "%choice%"=="3" goto both
if "%choice%"=="4" goto end

echo Invalid choice. Please try again.
timeout /t 2 >nul
goto menu

:client
echo Starting Minecraft Client...
start "" "run_client.bat"
if "%choice%"=="1" goto end
goto server

:server
echo Starting Minecraft Server...
start "" "run_server.bat"
if "%choice%"=="2" goto end
goto end

:both
echo Starting both Client and Server...
start "" "run_server.bat"
timeout /t 10 >nul
start "" "run_client.bat"
goto end

:end
if "%choice%"=="3" (
    echo Both Client and Server have been started.
    echo Close this window when you want to stop both.
    pause >nul
) else (
    exit
)