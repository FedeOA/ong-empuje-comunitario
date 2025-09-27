package com.grpc.demo.config;

import com.grpc.demo.auth.JwtAuthenticationFilter;
import com.grpc.demo.enums.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                        /**
                         * LOGIN
                         * Accesible para todos los usuarios sin autenticación
                         */
                        .requestMatchers("/api/auth/login").permitAll()

                        /**
                         * USERS
                         * - GET: accesible a PRESIDENTE y COORDINADOR
                         */
                        .requestMatchers(HttpMethod.GET, "/api/users/**")
                        .hasAnyRole(Role.PRESIDENTE.name(), Role.COORDINADOR.name())

                        /**
                         * USERS
                         * - Otros métodos (POST/PUT/DELETE): solo PRESIDENTE
                         */
                        .requestMatchers("/api/users/**")
                        .hasRole(Role.PRESIDENTE.name())

                        /**
                         * DONATIONS
                         * - Accesible a PRESIDENTE y VOCAL
                         */
                        .requestMatchers("/api/donations/**")
                        .hasAnyRole(Role.PRESIDENTE.name(), Role.VOCAL.name())

                        /**
                         * EVENTS
                         * - GET: accesible a PRESIDENTE, COORDINADOR y VOLUNTARIO
                         */
                        .requestMatchers(HttpMethod.GET, "/api/events/**")
                        .hasAnyRole(Role.PRESIDENTE.name(), Role.COORDINADOR.name(), Role.VOLUNTARIO.name())

                        /**
                         * EVENTS
                         * - POST /api/events/{eventId}/users/{username}:
                         *   PRESIDENTE, COORDINADOR y VOLUNTARIO (solo el mismo usuario vía @PreAuthorize)
                         */
                        .requestMatchers(HttpMethod.POST, "/api/events/*/users/*")
                        .hasAnyRole(Role.PRESIDENTE.name(), Role.COORDINADOR.name(), Role.VOLUNTARIO.name())

                        /**
                         * EVENTS
                         * - DELETE /api/events/{eventId}/users/{username}:
                         *   PRESIDENTE, COORDINADOR y VOLUNTARIO (solo el mismo usuario vía @PreAuthorize)
                         */
                        .requestMatchers(HttpMethod.DELETE, "/api/events/*/users/*")
                        .hasAnyRole(Role.PRESIDENTE.name(), Role.COORDINADOR.name(), Role.VOLUNTARIO.name())

                        /**
                         * EVENTS
                         * - Otros métodos sobre /api/events/**: solo PRESIDENTE y COORDINADOR
                         */
                        .requestMatchers("/api/events/**")
                        .hasAnyRole(Role.PRESIDENTE.name(), Role.COORDINADOR.name())

                        /**
                         * Cualquier otra request requiere autenticación
                         */
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
