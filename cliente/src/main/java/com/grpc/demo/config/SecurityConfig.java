package com.grpc.demo.config;

import com.grpc.demo.auth.JwtAuthenticationFilter;
import com.grpc.demo.enums.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers( "/api/users/**").hasRole(Role.PRESIDENTE.name())
                        .requestMatchers("/api/users/**", "/api/donations/**").hasAnyRole(Role.PRESIDENTE.name(),Role.VOCAL.name())
                        .requestMatchers("/api/events/**").hasAnyRole(Role.PRESIDENTE.name(), Role.COORDINADOR.name())
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}


