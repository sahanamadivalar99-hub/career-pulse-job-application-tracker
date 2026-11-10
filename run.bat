@echo off
setlocal enabledelayedexpansion
title Smart Job Application Tracker - Spring Boot

echo ===================================================================
echo   CareerPulse - Smart Job Application Tracker with AI Analyzer
echo ===================================================================

:: 1. Setup Java 21 and Maven paths
if exist "%~dp0.tools\jdk-21.0.5+11" (
    set "JAVA_HOME=%~dp0.tools\jdk-21.0.5+11"
) else (
    echo [INFO] Using system JAVA_HOME: %JAVA_HOME%
)
set "PATH=%JAVA_HOME%\bin;%~dp0.tools\apache-maven-3.9.9\bin;%PATH%"

echo [INFO] Java Version:
"%JAVA_HOME%\bin\java.exe" -version

:: 2. Check for lingering processes on port 8080 to prevent H2 file locks and port conflicts
echo.
echo [CHECK] Checking for existing instances on port 8080...
set "CONFLICT_PID="
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    set "CONFLICT_PID=%%a"
)

if defined CONFLICT_PID (
    echo [WARN] Found existing process (PID: !CONFLICT_PID!) using port 8080.
    echo [ACTION] Terminating lingering process !CONFLICT_PID! to release H2 database lock...
    taskkill /F /PID !CONFLICT_PID! >nul 2>&1
    timeout /t 2 /nobreak >nul
) else (
    echo [OK] Port 8080 is free. No conflicting instances found.
)

:: 3. Launch Application
echo.
echo ===================================================================
echo   Starting Spring Boot Server at http://localhost:8080
echo   Database: H2 (Auto-Server Mode, File-Persisted)
echo   Press Ctrl+C at any time to stop the server.
echo ===================================================================
echo.

if exist "%~dp0target\job-tracker-1.0.0.jar" (
    echo [INFO] Running packaged application (target\job-tracker-1.0.0.jar)...
    "%JAVA_HOME%\bin\java.exe" -jar "%~dp0target\job-tracker-1.0.0.jar"
) else (
    echo [INFO] JAR not found. Running with Maven spring-boot:run...
    call "%~dp0.tools\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
)

pause
