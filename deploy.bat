@echo off
REM Deploy SimpleChatWebSocket to Tomcat

set TOMCAT_HOME=D:\Dowload\TOMCAT\apache-tomcat-10.1.54-windows-x64\apache-tomcat-10.1.54
set WAR_FILE=%cd%\target\SimpleChatWebSocket.war
set WEBAPPS=%TOMCAT_HOME%\webapps

echo.
echo ====================================
echo Deploy SimpleChatWebSocket to Tomcat
echo ====================================
echo.

REM Kiem tra WAR file
if not exist "%WAR_FILE%" (
    echo ERROR: WAR file not found: %WAR_FILE%
    echo Please run: mvn clean package
    pause
    exit /b 1
)

echo [1/3] Stopping Tomcat...
call "%TOMCAT_HOME%\bin\shutdown.bat"
timeout /t 3

echo [2/3] Removing old deployment...
if exist "%WEBAPPS%\SimpleChatWebSocket" rmdir /s /q "%WEBAPPS%\SimpleChatWebSocket"
if exist "%WEBAPPS%\SimpleChatWebSocket.war" del "%WEBAPPS%\SimpleChatWebSocket.war"

echo [3/3] Deploying new WAR...
copy "%WAR_FILE%" "%WEBAPPS%\"

echo.
echo ====================================
echo Starting Tomcat...
echo ====================================
call "%TOMCAT_HOME%\bin\startup.bat"

echo.
echo ====================================
echo DONE! Application deployed.
echo ====================================
echo Access at: http://localhost:8080/SimpleChatWebSocket
echo WebSocket: ws://localhost:8080/SimpleChatWebSocket/chat
echo.
pause
