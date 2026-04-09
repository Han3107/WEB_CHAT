package com.simplechat;

import java.util.logging.Logger;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * Listener để log app initialization
 * Tomcat Embedded in Tomcat 10 auto-discovers @ServerEndpoint
 */
public class WebSocketListener implements ServletContextListener {
    
    private static final Logger LOGGER = Logger.getLogger(WebSocketListener.class.getName());
    
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        String contextPath = context.getContextPath();
        LOGGER.info("========================================");
        LOGGER.info("[App] SimpleChatWebSocket initialized");
        LOGGER.info("[App] Context Path: " + contextPath);
        LOGGER.info("[App] WebSocket URL: ws://localhost:8081" + contextPath + "/chat");
        LOGGER.info("========================================");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        LOGGER.info("[App] Application destroyed");
    }
}





