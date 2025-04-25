echo Clean workspace...
call gradlew clean

echo Building mod...
call gradlew build --stacktrace

if errorlevel 1 (
    echo Build failed
    pause
    exit /b 1
)

echo Starting Minecraft client...
call gradlew runClient

if errorlevel 1 (
    echo Failed to start Minecraft client
    pause
    exit /b 1
)
