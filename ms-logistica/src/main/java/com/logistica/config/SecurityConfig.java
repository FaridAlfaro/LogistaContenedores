package com.logistica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Endpoints internos para otros microservicios
                        .requestMatchers("/api/v1/tramos/**").authenticated()
                        // Endpoints de gestión para el OPERADOR
                        .requestMatchers("/api/v1/depositos/**").hasRole("OPERADOR")
                        .requestMatchers("/api/v1/tarifas/**").hasRole("OPERADOR")
                        .requestMatchers("/api/v1/rutas/**").hasRole("OPERADOR")
                        // Swagger
                        .requestMatchers("/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        return http.build();
    }
}