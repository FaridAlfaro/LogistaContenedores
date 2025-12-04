package com.logistica.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.logistica.client.OsrmDistanceResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OsrmClient2 {
    private final RestClient restClient;

    @SuppressWarnings("null")
    public OsrmClient2(@org.springframework.beans.factory.annotation.Value("${app.osrm.base-url}") String osrmUrl) {
        // OSRM local o remoto
        this.restClient = RestClient.builder().baseUrl(osrmUrl).build();
    }

    /**
     * Calcula distancia y tiempo entre dos puntos
     */
    public OsrmDistanceResponse calcularDistancia(Double lat1, Double lon1, Double lat2, Double lon2) {
        try {
            log.info("Calculando distancia: ({}, {}) -> ({}, {})", lat1, lon1, lat2, lon2);

            String uri = String.format("/route/v1/driving/%.6f,%.6f;%.6f,%.6f",
                    lon1, lat1, lon2, lat2);

            OsrmDistanceResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(OsrmDistanceResponse.class);

            log.info("Distancia calculada: {} km, {} segundos",
                    response.getDistanceKm(), response.getDurationSeconds());

            return response;
        } catch (Exception e) {
            log.error("Error al llamar OSRM", e);
            throw new RuntimeException("Error calculando distancia con OSRM: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene una lista de rutas alternativas entre dos puntos.
     * Cada elemento de la lista es un OsrmDistanceResponse que contiene una sola
     * ruta.
     */
    public java.util.List<OsrmDistanceResponse> getAlternativeRoutes(Double lat1, Double lon1, Double lat2,
            Double lon2) {
        try {
            log.info("Buscando rutas alternativas: ({}, {}) -> ({}, {})", lat1, lon1, lat2, lon2);

            String uri = String.format("/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=false&alternatives=3",
                    lon1, lat1, lon2, lat2);

            OsrmDistanceResponse fullResponse = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(OsrmDistanceResponse.class);

            java.util.List<OsrmDistanceResponse> resultList = new java.util.ArrayList<>();

            if (fullResponse != null && fullResponse.getRoutes() != null) {
                log.info("OSRM devolvió {} rutas", fullResponse.getRoutes().size());
                for (OsrmDistanceResponse.Route route : fullResponse.getRoutes()) {
                    OsrmDistanceResponse singleRouteResponse = new OsrmDistanceResponse();
                    singleRouteResponse.setRoutes(java.util.Collections.singletonList(route));
                    resultList.add(singleRouteResponse);
                }
            } else {
                log.warn("OSRM devolvió respuesta nula o sin rutas");
            }

            log.info("Se encontraron {} rutas alternativas", resultList.size());
            return resultList;

        } catch (Exception e) {
            log.error("Error al obtener rutas alternativas de OSRM", e);
            throw new RuntimeException("Error obteniendo rutas alternativas: " + e.getMessage(), e);
        }
    }
}
