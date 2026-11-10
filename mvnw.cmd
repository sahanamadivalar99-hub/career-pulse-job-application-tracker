@echo off
setlocal
if not defined JAVA_HOME (
    if exist "%~dp0.tools\jdk-21.0.5+11" (
        set "JAVA_HOME=%~dp0.tools\jdk-21.0.5+11"
    )
)
"%~dp0.tools\apache-maven-3.9.9\bin\mvn.cmd" %*
