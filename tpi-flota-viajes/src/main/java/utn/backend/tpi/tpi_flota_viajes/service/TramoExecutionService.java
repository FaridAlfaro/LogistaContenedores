package utn.backend.tpi.tpi_flota_viajes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utn.backend.tpi.tpi_flota_viajes.clients.LogisticaApiClient;
import utn.backend.tpi.tpi_flota_viajes.clients.dto.TramoDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class TramoExecutionService {

    private final LogisticaApiClient logisticaApiClient;
    private final CamionService camionService;

    /**
     * Inicia un tramo asignando un camión y notificando a ms-logistica
     *
     * @param idTramo ID del tramo a iniciar
     * @param dominioCamion Dominio del camión que ejecutará el tramo
     */
    @Transactional
    public TramoDTO iniciarTramo(Long idTramo, String dominioCamion) {
        log.info("Iniciando tramo {} para camión {}", idTramo, dominioCamion);

        // 1. Asignar el camión al tramo y cambiar su estado a EN_VIAJE
        camionService.asignarTramo(dominioCamion, idTramo);

        // 2. Notificar a ms-logistica que el tramo está iniciando
        // ms-logistica calculará distancias usando coordenadas de sus propios depósitos
        try {
            TramoDTO tramoActualizado = logisticaApiClient.iniciarTramo(idTramo);
            log.info("Tramo {} iniciado exitosamente en ms-logistica", idTramo);
            return tramoActualizado;
        } catch (Exception e) {
            log.error("Error al llamar a ms-logistica para iniciar tramo {}. Revirtiendo asignación de camión.", idTramo, e);
            throw new RuntimeException("Error en servicio de logística, no se pudo iniciar el tramo: " + e.getMessage(), e);
        }
    }

    /**
     * Finaliza un tramo liberando el camión y notificando a ms-logistica
     *
     * @param idTramo ID del tramo a finalizar
     * @param dominioCamion Dominio del camión que ejecutó el tramo
     * @param kmRecorridos Kilómetros recorridos durante el tramo
     */
    @Transactional
    public TramoDTO finalizarTramo(Long idTramo, String dominioCamion, double kmRecorridos) {
        log.info("Finalizando tramo {} para camión {}. KMs recorridos: {}",
                idTramo, dominioCamion, kmRecorridos);

        // 1. Notificar a ms-logistica que el tramo está finalizando
        TramoDTO tramoActualizado;
        try {
            tramoActualizado = logisticaApiClient.finalizarTramo(idTramo, kmRecorridos);
            log.info("Tramo {} finalizado exitosamente en ms-logistica", idTramo);
        } catch (Exception e) {
            log.error("Error al llamar a ms-logistica para finalizar tramo {}. No se liberará el camión.", idTramo, e);
            throw new RuntimeException("Error en servicio de logística, no se pudo finalizar el tramo: " + e.getMessage(), e);
        }

        // 2. Si exitoso en ms-logistica, liberar el camión localmente
        camionService.liberarCamion(dominioCamion, idTramo, kmRecorridos);

        return tramoActualizado;
    }
}

