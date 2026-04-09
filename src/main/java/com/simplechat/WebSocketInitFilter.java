package com.simplechat;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * Filter để register WebSocket endpoint khi first request comes in
 * (NOT registered - using @ServerEndpoint instead)
 */
// @WebFilter(urlPatterns = "/*")
public class WebSocketInitFilter implements Filter {
    
    private static final Logger LOGGER = Logger.getLogger(WebSocketInitFilter.class.getName());
    private static boolean initialized = false;
    
    @Override
    public void init(FilterConfig config) throws ServletException {
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!initialized) {
            registerEndpoint(request);
            initialized = true;
        }
        chain.doFilter(request, response);
    }
    
    private synchronized void registerEndpoint(ServletRequest request) {
        if (initialized) return;
        
        try {
            var context = request.getServletContext();
            String contextPath = context.getContextPath();
            
            LOGGER.info("========================================");
            LOGGER.info("[Filter] Starting WebSocket endpoint registration");
            
            // Get ServerContainer
            Object container = context.getAttribute("jakarta.websocket.server.ServerContainer");
            
            if (container == null) {
                LOGGER.warning("[Filter] jakarta.websocket.server.ServerContainer is null, trying javax namespace...");
                container = context.getAttribute("javax.websocket.server.ServerContainer");
            }
            
            if (container != null) {
                LOGGER.info("[Filter] ServerContainer found: " + container.getClass().getName());
                LOGGER.info("[Filter] Available methods: ");
                for (var method : container.getClass().getMethods()) {
                    if (method.getName().contains("endpoint")) {
                        LOGGER.info("  - " + method.getName() + " params: " + method.getParameterCount());
                    }
                }
                
                try {
                    var addEndpointMethod = container.getClass().getMethod("addEndpoint", Class.class);
                    Object result = addEndpointMethod.invoke(container, ChatEndpoint.class);
                    LOGGER.info("[SUCCESS] WebSocket endpoint registered! Result: " + result);
                    LOGGER.info("[SUCCESS] ws://localhost:8080" + contextPath + "/chat");
                } catch (NoSuchMethodException e) {
                    LOGGER.warning("[Filter] addEndpoint method not found");
                    LOGGER.warning("[Filter] Actual methods: ");
                    for (var method : container.getClass().getMethods()) {
                        LOGGER.warning("  - " + method.getName());
                    }
                }
            } else {
                LOGGER.warning("[Filter] ServerContainer still not available");
            }
            
            LOGGER.info("========================================");
        } catch (Exception e) {
            LOGGER.severe("[ERROR] Registration failed: " + e.getClass().getName() + " - " + e.getMessage());
            if (e.getCause() != null) {
                LOGGER.severe("[CAUSE] " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
            }
            StringWriter sw = new StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            LOGGER.severe(sw.toString());
        }
    }
    
    @Override
    public void destroy() {
    }
}
