package com.simplechat;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * Legacy pass-through filter.
 * Endpoint registration is handled by @ServerEndpoint container scanning.
 */
// @WebFilter(urlPatterns = "/*")
public class WebSocketInitFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
    }
}
