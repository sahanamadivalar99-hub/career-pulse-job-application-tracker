@echo off
setlocal
echo ========================================================
echo Building Smart Job Application Tracker
echo ========================================================

set "JAVA_HOME=%~dp0.tools\jdk-21.0.5+11"
set "PATH=%JAVA_HOME%\bin;%~dp0.tools\apache-maven-3.9.9\bin;%PATH%"

"%~dp0.tools\apache-maven-3.9.9\bin\mvn.cmd" clean package -DskipTests=false

pause
