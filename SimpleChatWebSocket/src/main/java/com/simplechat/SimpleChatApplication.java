package com.simplechat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.simplechat.security.AuthorizationInterceptor;

@SpringBootApplication()
@ComponentScan(basePackages = {"com.simplechat"})
public class SimpleChatApplication extends SpringBootServletInitializer implements WebMvcConfigurer {
    
    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(SimpleChatApplication.class);
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizationInterceptor)
               .addPathPatterns("/api/**")
               .excludePathPatterns("/api/auth/login", "/api/auth/signup");
    }
    
    public static void main(String[] args) {
        SpringApplication.run(SimpleChatApplication.class, args);
        System.out.println("=========================================");
        System.out.println("Simple Chat Application Started!");
        System.out.println("Access: http://localhost:8080/SimpleChatWebSocket/");
        System.out.println("API: http://localhost:8080/SimpleChatWebSocket/api");
        System.out.println("=========================================");
    }
}
