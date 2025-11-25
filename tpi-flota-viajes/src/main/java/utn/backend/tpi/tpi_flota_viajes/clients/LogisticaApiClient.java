package utn.backend.tpi.tpi_flota_viajes.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import jakarta.ws.rs.BadRequestException;
import utn.backend.tpi.tpi_flota_viajes.clients.dto.TramoDTO;

@Component
public class LogisticaApiClient {
    private final RestClient restClient;

    public LogisticaApiClient(@Value("${api.logistica.url}") String logisticaBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(logisticaBaseUrl).build();
    }

    public TramoDTO iniciarTramo(Long idTramo) {
        return restClient.put()
                .uri("/api/v1/tramos/{id}/iniciar", idTramo)
                .retrieve()
                // Agregar manejo de errores específico si es necesario
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new BadRequestException("Error en solicitud a Logística: " + response.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Error en servidor de Logística: " + response.getStatusText());
                })
                .body(TramoDTO.class);
    }

    /**
     * Notifica a ms-logistica que un tramo está iniciando
     * ms-logistica calculará distancias usando coordenadas de sus propios depósitos
     */

    /**
     * Notifica a ms-logistica que un tramo está finalizando
     * Envía los kilómetros recorridos para registro
     */
    public TramoDTO finalizarTramo(Long idTramo, double kmRecorridos) {
        return restClient.put()
                .uri("/api/v1/tramos/{id}/finalizar?kmRecorridos={kmRecorridos}", idTramo, kmRecorridos)
                .retrieve()
                .body(TramoDTO.class);
    }

    public void asignarCamion(Long idTramo, String dominio) {
        restClient.post()
                .uri("/api/v1/tramos/{id}/asignar?dominio={dominio}", idTramo, dominio)
                .retrieve()
                .toBodilessEntity();
    }
}