package com.simplechat.config;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * SecurityConfig - Cấu hình bảo mật và mã hóa mật khẩu
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    
    /**
     * BCryptPasswordEncoder bean - dùng để mã hóa mật khẩu
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * SecurityFilterChain - Cấu hình HTTP security
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (vì dùng API)
            .csrf(csrf -> csrf.disable())
            
            // CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Authorization config
            .authorizeHttpRequests(auth -> auth
                // Allow public access to login pages
                .requestMatchers(
                    new AntPathRequestMatcher("/login.html"),
                    new AntPathRequestMatcher("/login-simple.html"),
                    new AntPathRequestMatcher("/index.html"),
                    new AntPathRequestMatcher("/404.html"),
                    new AntPathRequestMatcher("/admin-dashboard.html"),
                    new AntPathRequestMatcher("/chat.html")
                ).permitAll()
                // Allow public access to auth API (with and without context path)
                .requestMatchers(
                    new AntPathRequestMatcher("/api/auth/**"),
                    new AntPathRequestMatcher("/SimpleChatWebSocket/api/auth/**")
                ).permitAll()
                // Allow public access to admin API (with and without context path)
                .requestMatchers(
                    new AntPathRequestMatcher("/api/admin/**"),
                    new AntPathRequestMatcher("/SimpleChatWebSocket/api/admin/**")
                ).permitAll()
                // Allow public access to users API (with and without context path)
                .requestMatchers(
                    new AntPathRequestMatcher("/api/users/**"),
                    new AntPathRequestMatcher("/SimpleChatWebSocket/api/users/**")
                ).permitAll()
                // Allow static resources
                .requestMatchers(
                    new AntPathRequestMatcher("/css/**"),
                    new AntPathRequestMatcher("/js/**"),
                    new AntPathRequestMatcher("/images/**"),
                    new AntPathRequestMatcher("/fonts/**")
                ).permitAll()
                // Allow WebSocket
                .requestMatchers(
                    new AntPathRequestMatcher("/ws/**"),
                    new AntPathRequestMatcher("/app/**"),
                    new AntPathRequestMatcher("/topic/**")
                ).permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Disable form login (dùng custom API login)
            .formLogin(form -> form.disable())
            
            // Disable HTTP Basic
            .httpBasic(httpBasic -> httpBasic.disable())
            
            // Allow stateless (API mode)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
            )
            
            // Enable anonymous access
            .anonymous(anon -> anon.authorities("ROLE_ANONYMOUS"))
            
            // Exception handling - return JSON for API, redirect for HTML pages
            .exceptionHandling(ex -> 
                ex.authenticationEntryPoint((request, response, exception) -> {
                    String requestUri = request.getRequestURI();
                    logger.warn("⚠️ AuthenticationEntryPoint triggered for: " + requestUri);
                    // For API requests, return JSON 401
                    if (requestUri.contains("/api/")) {
                        logger.info("📤 Sending JSON 401 for API: " + requestUri);
                        response.setContentType("application/json");
                        response.setStatus(401);
                        response.getWriter().write("{\"message\":\"Unauthorized\",\"code\":\"UNAUTHORIZED\"}");
                    } else {
                        // For page requests, redirect to login
                        logger.info("📤 Redirecting to login for: " + requestUri);
                        response.sendRedirect("/SimpleChatWebSocket/login.html");
                    }
                })
            );
        
        return http.build();
    }
    
    /**
     * CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
