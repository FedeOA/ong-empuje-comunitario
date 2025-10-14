package com.ong.empuje.comunitario.web_services.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Configure CORS using the defined CorsConfigurationSource
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Disable CSRF as per original configuration
            .csrf(csrf -> csrf.disable())
            // Allow all requests without authentication
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);
            config.addAllowedOrigin("http://localhost:5173");
            config.addAllowedHeader("Authorization");
            config.addAllowedHeader("Content-Type");
            config.addAllowedHeader("Accept");
            config.addAllowedHeader("Apollo-Require-Preflight");
            config.addAllowedMethod("GET");
            config.addAllowedMethod("POST");
            config.addAllowedMethod("OPTIONS");
            config.setMaxAge(3600L);
            return config;
        };
    }
}