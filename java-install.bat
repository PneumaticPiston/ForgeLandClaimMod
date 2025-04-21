@echo off
echo Downloading Eclipse Temurin JDK 17...

:: Create downloads directory
mkdir "%USERPROFILE%\Downloads\Java" 2>nul

:: Download Eclipse Temurin JDK 17
powershell -Command "& {Invoke-WebRequest -Uri 'https://download.eclipse.org/adoptium/17/jdk/x64/windows/OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.msi' -OutFile '%USERPROFILE%\Downloads\Java\temurin17.msi'}"

:: Install silently
msiexec /i "%USERPROFILE%\Downloads\Java\temurin17.msi" ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith,FeatureJavaHome /quiet

echo Installation complete! Please restart your terminal.
pause