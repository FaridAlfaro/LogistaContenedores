package com.transporte.ms_solicitudes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable) // Forma moderna de deshabilitar CSRF
                                .authorizeHttpRequests(authorize -> authorize
                                                // --- Rutas Públicas ---
                                                .requestMatchers("/swagger-ui.html", "/v3/api-docs/**",
                                                                "/swagger-ui/**")
                                                .permitAll()
                                                // Auto-registro de clientes (Público)
                                                .requestMatchers(HttpMethod.POST, "/api/v1/solicitudes").permitAll()

                                                // --- Rutas de OPERADOR ---
                                                // Nota: hasRole("OPERADOR") busca internamente la authority
                                                // "ROLE_OPERADOR"
                                                .requestMatchers(HttpMethod.GET, "/api/v1/solicitudes/pendientes")
                                                .hasRole("OPERADOR")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/solicitudes/*/aceptar")
                                                .hasRole("OPERADOR")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/solicitudes/*/en-transito")
                                                .hasRole("OPERADOR") // Agregado por consistencia
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/solicitudes/*/entregada")
                                                .hasRole("OPERADOR") // Agregado por consistencia
                                                .requestMatchers(HttpMethod.POST, "/api/v1/contenedores")
                                                .hasRole("OPERADOR")

                                                // --- Rutas de CLIENTE ---
                                                .requestMatchers(HttpMethod.GET, "/api/v1/solicitudes/*")
                                                .hasRole("CLIENTE")

                                                // Todo lo demás requiere autenticación genérica
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                jwtAuthenticationConverter())));

                return http.build();
        }

        /**
         * Convierte los roles de Keycloak (que vienen en 'realm_access')
         * a autoridades de Spring Security con el formato 'ROLE_NOMBREENMAYUSCULA'.
         */
        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

                // Definimos el convertidor de autoridades usando una Lambda
                converter.setJwtGrantedAuthoritiesConverter(jwt -> {
                        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

                        if (realmAccess == null || !realmAccess.containsKey("roles")) {
                                return Collections.emptyList();
                        }

                        @SuppressWarnings("unchecked")
                        List<String> roles = (List<String>) realmAccess.get("roles");

                        return roles.stream()
                                        .map(role -> "ROLE_" + role.toUpperCase()) // Transforma 'operador' ->
                                                                                   // 'ROLE_OPERADOR'
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList());
                });

                return converter;
        }
}