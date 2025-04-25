@echo off
echo Running gradlew build with --scan for detailed output...
echo This will help diagnose any build issues.

:: First, try to run with --scan
call gradlew build --scan

:: If it failed and was a Gson issue, try cleaning
if %ERRORLEVEL% NEQ 0 (
    echo First build attempt failed, trying with clean...
    call gradlew clean
    
    echo Now trying to build again with stacktrace...
    call gradlew build --stacktrace
)

pause