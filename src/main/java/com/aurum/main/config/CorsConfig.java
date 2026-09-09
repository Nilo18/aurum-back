package com.aurum.main.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Match all endpoints in your app
                        .allowedOrigins("http://localhost:4200"/*, "https://yourfrontend.com" */) // Allowed domains
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods
                        .allowedHeaders("*") // Allow all headers (or specify like "Content-Type", "Authorization")
                        .exposedHeaders("Authorization") // Headers accessible to the client browser
                        .allowCredentials(true) // Set to true if you pass cookies or session auth tokens
                        .maxAge(3600); // Cache preflight response for 1 hour
            }
        };
    }
}
