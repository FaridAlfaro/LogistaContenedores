package utn.backend.tpi.tpi_flota_viajes.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import utn.backend.tpi.tpi_flota_viajes.clients.dto.TramoDTO;

@Component
public class LogisticaApiClient {
    private final RestClient restClient;

    public LogisticaApiClient(@Value("${api.logistica.url}") String logisticaBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(logisticaBaseUrl).build();
    }

    /**
     * Notifica a ms-logistica que un tramo está iniciando
     * ms-logistica calculará distancias usando coordenadas de sus propios depósitos
     */
    public TramoDTO iniciarTramo(Long idTramo) {
        return restClient.put()
                .uri("/api/v1/tramos/{id}/iniciar", idTramo)
                .retrieve()
                .body(TramoDTO.class);
    }

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
}