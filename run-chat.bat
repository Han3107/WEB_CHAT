@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Starting SimpleChatWebSocket...
echo ========================================

REM Fix invalid Java environment (common cause of Tomcat startup failure)
set "JRE_HOME="
set "JAVA_EXE="
for /f "delims=" %%I in ('where java 2^>nul') do (
	set "JAVA_EXE=%%I"
	goto :JAVA_FOUND
)

echo ERROR: Java not found in PATH. Please install JDK and try again.
exit /b 1

:JAVA_FOUND
for %%I in ("!JAVA_EXE!") do set "JAVA_DIR=%%~dpI"
for %%I in ("!JAVA_DIR!..") do set "JRE_HOME=%%~fI"

REM Navigate to Tomcat bin directory
cd /d "D:\Dowload\TOMCAT\apache-tomcat-10.1.54-windows-x64\apache-tomcat-10.1.54\bin"

REM Start Tomcat
echo Starting Tomcat...
call startup.bat

REM Wait for Tomcat to initialize
echo Waiting for server to start (10 seconds)...
timeout /t 10 /nobreak

REM Open browser
echo.
echo Opening SimpleChatWebSocket in browser...
start http://localhost:8080/SimpleChatWebSocket/

echo ========================================
echo Done! Browser should be opening now
echo ========================================
