package com.logistica.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class FlotaApiClient {
    private final RestClient restClient;

    public FlotaApiClient(@Value("${app.flota.base-url:http://ms-flota:8083}") String flotaBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(flotaBaseUrl)
                .build();
    }

    /**
     * Obtiene información de un camión por su dominio
     */
    public CamionInfo obtenerCamionPorDominio(String dominio) {
        try {
            log.info("Obteniendo información del camión con dominio: {}", dominio);
            
            CamionInfo camion = restClient.get()
                    .uri("/api/flota/camiones/dominio/{dominio}", dominio)
                    .retrieve()
                    .body(CamionInfo.class);
            
            if (camion == null) {
                throw new RuntimeException("Camión no encontrado: " + dominio);
            }
            
            log.info("Camión obtenido: dominio={}, costoPorKm={}, consumo={}", 
                    camion.getDominio(), camion.getCostoPorKm(), camion.getConsumoCombustiblePromedio());
            
            return camion;
        } catch (Exception e) {
            log.error("Error al obtener camión {}: {}", dominio, e.getMessage(), e);
            throw new RuntimeException("Error al obtener información del camión: " + e.getMessage(), e);
        }
    }

    @Data
    public static class CamionInfo {
        private Long id;
        private String dominio;
        private Double capacidadPeso;
        private Double capacidadVolumen;
        private Double consumoCombustiblePromedio;
        private Double costoPorKm;
    }
}

