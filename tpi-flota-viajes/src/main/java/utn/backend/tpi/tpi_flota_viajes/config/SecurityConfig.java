package utn.backend.tpi.tpi_flota_viajes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Permitir acceso a Swagger UI
                .requestMatchers("/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                
                // Endpoints de FLOTA (Camiones, Transportistas) para ROL OPERADOR
                .requestMatchers(HttpMethod.POST, "/api/flota/camiones").hasRole("OPERADOR")
                .requestMatchers(HttpMethod.GET, "/api/flota/camiones/disponibles").hasRole("OPERADOR")
                .requestMatchers(HttpMethod.POST, "/api/flota/transportistas").hasRole("OPERADOR")

                // Endpoints de VIAJES (Iniciar/Finalizar) para ROL TRANSPORTISTA
                // (Se validará por @PreAuthorize en el controlador, pero podemos ponerlo aquí también)
                .requestMatchers("/api/flota/tramos/**").hasRole("TRANSPORTISTA")

                // Denegar todo lo demás por defecto
                .anyRequest().authenticated()
            )
            // Usar validación de token JWT (Resource Server)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        
        return http.build();
    }
}