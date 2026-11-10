@echo off
setlocal enabledelayedexpansion
title Smart Job Application Tracker - MySQL Profile

echo ===================================================================
echo   CareerPulse - Smart Job Application Tracker (MySQL Profile)
echo ===================================================================

if exist "%~dp0.tools\jdk-21.0.5+11" (
    set "JAVA_HOME=%~dp0.tools\jdk-21.0.5+11"
)
set "PATH=%JAVA_HOME%\bin;%~dp0.tools\apache-maven-3.9.9\bin;%PATH%"

echo.
echo [CHECK] Checking for existing instances on port 8080...
set "CONFLICT_PID="
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    set "CONFLICT_PID=%%a"
)

if defined CONFLICT_PID (
    echo [WARN] Found existing process (PID: !CONFLICT_PID!) using port 8080.
    taskkill /F /PID !CONFLICT_PID! >nul 2>&1
    timeout /t 2 /nobreak >nul
)

echo.
echo [INFO] Starting Spring Boot with MySQL profile at http://localhost:8080...
echo [INFO] Ensure MySQL server is running on port 3306.
echo.

if exist "%~dp0target\job-tracker-1.0.0.jar" (
    "%JAVA_HOME%\bin\java.exe" -jar "%~dp0target\job-tracker-1.0.0.jar" --spring.profiles.active=mysql
) else (
    call "%~dp0.tools\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.profiles=mysql
)

pause
