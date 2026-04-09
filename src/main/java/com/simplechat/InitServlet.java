package com.simplechat;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet chạy khi request đầu tiên để register WebSocket endpoint
 */
@WebServlet(urlPatterns = "/init")
public class InitServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(InitServlet.class.getName());
    private static final long serialVersionUID = 1L;
    private static boolean registered = false;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!registered) {
            registerWebSocketEndpoint();
            registered = true;
        }
        resp.setContentType("text/plain");
        resp.getWriter().println("WebSocket endpoint registration done");
    }
    
    private void registerWebSocketEndpoint() {
        try {
            var context = getServletContext();
            String contextPath = context.getContextPath();
            
            LOGGER.info("========================================");
            LOGGER.info("[Init] Registering WebSocket endpoint");
            LOGGER.info("[Init] Context Path: " + contextPath);
            
            // Get ServerContainer
            Object serverContainer = context.getAttribute("jakarta.websocket.server.ServerContainer");
            
            if (serverContainer != null) {
                try {
                    // Try to add endpoint
                    var addEndpointMethod = serverContainer.getClass().getMethod("addEndpoint", Class.class);
                    addEndpointMethod.invoke(serverContainer, ChatEndpoint.class);
                    LOGGER.info("[SUCCESS] WebSocket ChatEndpoint registered!");
                    LOGGER.info("[SUCCESS] URL: ws://localhost:8081" + contextPath + "/chat");
                } catch (NoSuchMethodException e) {
                    LOGGER.warning("[WARNING] addEndpoint method not found: " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    LOGGER.severe("[ERROR] Failed to invoke addEndpoint: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                LOGGER.warning("[WARNING] ServerContainer not found");
            }
            
            LOGGER.info("========================================");
        } catch (Exception e) {
            LOGGER.severe("[ERROR] Failed to register endpoint: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
