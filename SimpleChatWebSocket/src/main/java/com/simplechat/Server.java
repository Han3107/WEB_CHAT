package com.simplechat;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.Set;

import jakarta.websocket.Encoder;
import jakarta.websocket.Decoder;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpoint;

import jakarta.servlet.ServletContext;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.scan.StandardJarScanner;

/**
 * Embedded Tomcat Server - Chạy SimpleChatWebSocket
 * Just run: mvn exec:java -Dexec.mainClass="com.simplechat.Server"
 */
public class Server {
    public static void main(String[] args) throws Exception {
        // Tạo Tomcat instance
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8081);  // Use port 8081
        
        // Set base directory to tmp to avoid permission issues
        File baseDir = new File("./tomcat-temp");
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        tomcat.setBaseDir(baseDir.getAbsolutePath());
        
        // Add web application
        File warFile = new File("target/SimpleChatWebSocket.war");
        if (!warFile.exists()) {
            System.err.println("ERROR: WAR file not found: " + warFile.getAbsolutePath());
            System.err.println("Please run: mvn clean package");
            System.exit(1);
        }
        
        Context context = tomcat.addWebapp(
            "", 
            warFile.getAbsolutePath()
        );
        context.setReloadable(true);
        
        // Enable JAR scanner for classpath annotation discovery
        context.setJarScanner(new StandardJarScanner());
        
        // Thêm Lifecycle Listener để verify endpoint registration
        context.addLifecycleListener(new org.apache.catalina.LifecycleListener() {
            @Override
            public void lifecycleEvent(org.apache.catalina.LifecycleEvent event) {
                if (event.getType().equals("configure_start")) {
                    verifyWebSocketEndpoint(context);
                }
            }
        });
        
        System.out.println();
        System.out.println("=" .repeat(50));
        System.out.println("SimpleChatWebSocket - Embedded Tomcat");
        System.out.println("=" .repeat(50));
        System.out.println();
        
        // Start Tomcat
        tomcat.start();
        
        // Delay and verify endpoint in separate thread
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                verifyWebSocketEndpoint(context);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        System.out.println();
        System.out.println("🌐 Web URL:  http://localhost:8081");
        System.out.println("💬 WebSocket: ws://localhost:8081/chat");
        System.out.println();
        System.out.println("Opening browser...");
        System.out.println();
        
        // Mở browser tự động
        openBrowser("http://localhost:8081");
        
        System.out.println("Press CTRL+C to stop...");
        System.out.println();
        
        // Keep running
        tomcat.getServer().await();
    }
    
    /**
     * Verify WebSocket endpoint is registered
     */
    private static void verifyWebSocketEndpoint(Context context) {
        try {
            ServletContext servletContext = context.getServletContext();
            if (servletContext == null) {
                System.err.println("[✗] ServletContext is null");
                return;
            }
            
            Object attr = servletContext.getAttribute("jakarta.websocket.server.ServerContainer");
            if (attr instanceof ServerContainer serverContainer) {
                // Endpoint should be auto-discovered, just verify it's registered
                System.out.println("[✓] WebSocket ServerContainer found and configured");
            } else {
                System.err.println("[✗] ServerContainer not found or not instanceof ServerContainer");
                System.err.println("    Attribute value: " + attr);
            }
        } catch (Exception e) {
            System.err.println("[✗] Error verifying WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Mở URL trong browser mặc định
     */
    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Nếu Desktop API không hoạt động, dùng Runtime
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec("cmd /c start " + url);
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec("open " + url);
                } else {
                    Runtime.getRuntime().exec("xdg-open " + url);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Could not open browser: " + e.getMessage());
        }
    }
}
