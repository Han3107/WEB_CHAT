# Deploy SimpleChatWebSocket to Tomcat

$TOMCAT_HOME = "D:\Dowload\TOMCAT\apache-tomcat-10.1.54-windows-x64\apache-tomcat-10.1.54"
$WAR_FILE = "d:\HK225\LAP_TRINH_MANG\THUC_HANH\CHAT\WEB_CHAT\SimpleChatWebSocket\target\SimpleChatWebSocket.war"
$WEBAPPS = "$TOMCAT_HOME\webapps"

Write-Host "===================================="
Write-Host "Deploy SimpleChatWebSocket to Tomcat"
Write-Host "===================================="
Write-Host ""

# Check WAR file
if (-not (Test-Path $WAR_FILE)) {
    Write-Host "ERROR: WAR file not found: $WAR_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "[1/3] Stopping Tomcat..."
& "$TOMCAT_HOME\bin\shutdown.bat"
Start-Sleep -Seconds 3

Write-Host "[2/3] Removing old deployment..."
if (Test-Path "$WEBAPPS\SimpleChatWebSocket") {
    Remove-Item -Path "$WEBAPPS\SimpleChatWebSocket" -Recurse -Force
}
if (Test-Path "$WEBAPPS\SimpleChatWebSocket.war") {
    Remove-Item -Path "$WEBAPPS\SimpleChatWebSocket.war" -Force
}

Write-Host "[3/3] Copying WAR file..."
Copy-Item -Path $WAR_FILE -Destination "$WEBAPPS\" -Force

Write-Host ""
Write-Host "===================================="
Write-Host "Starting Tomcat..."
Write-Host "===================================="
& "$TOMCAT_HOME\bin\startup.bat"

Write-Host ""
Write-Host "===================================="
Write-Host "DONE!"
Write-Host "===================================="
Write-Host "Access at: http://localhost:8080/SimpleChatWebSocket"
Write-Host "WebSocket: ws://localhost:8080/SimpleChatWebSocket/chat"
Write-Host ""
