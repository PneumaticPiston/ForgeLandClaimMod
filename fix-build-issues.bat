@echo off
echo This script will attempt to fix common build issues.

:: 0. Check if Java 17 is available
echo Checking for Java installation...
java -version 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Java not found or not in PATH
    echo Running Java installation script...
    call java-install.bat
    echo Please restart your computer after Java installation and try building again.
    goto :end
)

:: 1. Clean the project
echo Cleaning project...
call gradlew clean --refresh-dependencies

:: 2. Check Java version being used
echo Checking Java configuration...
call gradlew showJavaInfo

:: 3. Try to build with refresh dependencies
echo Attempting build with refreshed dependencies...
call gradlew build --stacktrace

:: 4. If still failing, try to suggest manual fixes
if %ERRORLEVEL% NEQ 0 (
    echo Build is still failing. Here are some manual steps to try:
    echo 1. Make sure Java 17 is installed and in your PATH
    echo 2. Try editing gradle.properties and setting org.gradle.java.home to your JDK 17 location
    echo 3. Try deleting the .gradle folder in your user directory
    echo 4. Try running 'gradlew --stop' to stop any running daemons
)

:end
pause