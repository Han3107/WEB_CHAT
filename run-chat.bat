@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Starting SimpleChatWebSocket...
echo ========================================

REM Navigate to Tomcat bin directory
cd /d "D:\Dowload\TOMCAT\apache-tomcat-10.1.54-windows-x64\apache-tomcat-10.1.54\bin"

REM Start Tomcat
echo Starting Tomcat...
start /B catalina.bat run

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
