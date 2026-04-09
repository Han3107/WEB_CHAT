import os
import shutil
import subprocess
import time
import webbrowser

TOMCAT_HOME = r"D:\Dowload\TOMCAT\apache-tomcat-10.1.54-windows-x64\apache-tomcat-10.1.54"
WAR_FILE = r"D:\HK225\PHAT\SimpleChatWebSocket\target\SimpleChatWebSocket.war"
WEBAPPS = os.path.join(TOMCAT_HOME, "webapps")
STARTUP_BAT = os.path.join(TOMCAT_HOME, "bin", "startup.bat")
SHUTDOWN_BAT = os.path.join(TOMCAT_HOME, "bin", "shutdown.bat")

print("=" * 50)
print("SimpleChatWebSocket Auto-Deploy")
print("=" * 50)
print()

# 1. Check WAR file
if not os.path.exists(WAR_FILE):
    print(f"❌ ERROR: WAR file not found: {WAR_FILE}")
    exit(1)
print(f"✓ WAR file found: {WAR_FILE}")
print()

# 2. Stop Tomcat
print("[1/4] Stopping Tomcat...")
try:
    subprocess.run(SHUTDOWN_BAT, shell=True, timeout=5)
    time.sleep(2)
except:
    pass
print("✓ Stopped")
print()

# 3. Remove old deployment
print("[2/4] Removing old deployment...")
old_dir = os.path.join(WEBAPPS, "SimpleChatWebSocket")
old_war = os.path.join(WEBAPPS, "SimpleChatWebSocket.war")

if os.path.exists(old_dir):
    shutil.rmtree(old_dir)
    print(f"✓ Removed: {old_dir}")
    
if os.path.exists(old_war):
    os.remove(old_war)
    print(f"✓ Removed: {old_war}")
print()

# 4. Deploy new WAR
print("[3/4] Deploying new WAR...")
shutil.copy(WAR_FILE, WEBAPPS)
print(f"✓ Copied to: {WEBAPPS}")
print()

# 5. Start Tomcat
print("[4/4] Starting Tomcat...")
subprocess.Popen(STARTUP_BAT, shell=True)
print("✓ Tomcat started")
print()

# Wait for Tomcat to start
print("⏳ Waiting for Tomcat to start (10 seconds)...")
time.sleep(10)

print()
print("=" * 50)
print("✅ SUCCESS! Application is running!")
print("=" * 50)
print()
print("🌐 Web URL: http://localhost:8080/SimpleChatWebSocket")
print("💬 WebSocket: ws://localhost:8080/SimpleChatWebSocket/chat")
print()

# Open browser
try:
    webbrowser.open("http://localhost:8080/SimpleChatWebSocket")
    print("🔗 Opening browser...")
except:
    pass

print("Ready to chat! 💬")
