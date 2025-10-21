package com.ong.empuje.comunitario.web_services.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173"); // Explicit origin
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("Apollo-Require-Preflight");
        config.addExposedHeader("Access-Control-Allow-Origin");
        config.addExposedHeader("Access-Control-Allow-Credentials");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("OPTIONS");
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/graphql/**", config);
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/graphql/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("Authorization", "Content-Type", "Accept", "Apollo-Require-Preflight")
                        .exposedHeaders("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")
                        .allowCredentials(true)
                        .maxAge(3600);

                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("Authorization", "Content-Type", "Accept")
                        .exposedHeaders("Content-Disposition")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}