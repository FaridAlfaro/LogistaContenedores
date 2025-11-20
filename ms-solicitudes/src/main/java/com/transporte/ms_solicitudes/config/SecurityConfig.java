package com.transporte.ms_solicitudes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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
                // Permitir acceso anónimo a Swagger UI [cite: 115]
                .requestMatchers("/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll()

                // Requerimiento: POST /solicitudes es para 'Cliente' [cite: 40]
                .requestMatchers(HttpMethod.POST, "/api/v1/solicitudes").hasRole("CLIENTE")

                // Requerimiento: GET /solicitudes/{nro} es para 'Cliente' [cite: 30]
                .requestMatchers(HttpMethod.GET, "/api/v1/solicitudes/*").hasRole("CLIENTE")

                // Requerimiento: GET /solicitudes/pendientes es para 'Operador' [cite: 48, 49]
                .requestMatchers(HttpMethod.GET, "/api/v1/solicitudes/pendientes").hasRole("OPERADOR")

                // Requerimiento: PUT /solicitudes/{nro}/aceptar es para 'Operador'
                .requestMatchers(HttpMethod.PUT, "/api/v1/solicitudes/*/aceptar").hasRole("OPERADOR")

                // Denegar todo lo demás por defecto
                .anyRequest().authenticated()
            )
            // Usar validación de token JWT (Resource Server)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}