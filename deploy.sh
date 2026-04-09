#!/bin/bash
# Auto-deploy SimpleChatWebSocket to Tomcat

TOMCAT_HOME="D:\Dowload\TOMCAT\apache-tomcat-10.1.54-windows-x64\apache-tomcat-10.1.54"
WAR_FILE="target/SimpleChatWebSocket.war"
WEBAPPS="$TOMCAT_HOME/webapps"

echo "===================================="
echo "SimpleChatWebSocket Auto-Deploy"
echo "===================================="
echo ""

# Build
echo "[1/4] Building project..."
mvn clean package -q
if [ $? -ne 0 ]; then
    echo "ERROR: Build failed!"
    exit 1
fi
echo "✓ Build success"
echo ""

# Deploy
echo "[2/4] Stopping Tomcat..."
"$TOMCAT_HOME/bin/shutdown.sh" 2>/dev/null
sleep 2
echo "✓ Tomcat stopped"
echo ""

echo "[3/4] Deploying WAR..."
rm -rf "$WEBAPPS/SimpleChatWebSocket"
rm -f "$WEBAPPS/SimpleChatWebSocket.war"
cp "$WAR_FILE" "$WEBAPPS/"
echo "✓ WAR deployed"
echo ""

echo "[4/4] Starting Tomcat..."
"$TOMCAT_HOME/bin/startup.sh" &
sleep 3
echo "✓ Tomcat started"
echo ""

echo "===================================="
echo "SUCCESS!"
echo "===================================="
echo "Access at: http://localhost:8080/SimpleChatWebSocket"
echo "WebSocket: ws://localhost:8080/SimpleChatWebSocket/chat"
echo ""
