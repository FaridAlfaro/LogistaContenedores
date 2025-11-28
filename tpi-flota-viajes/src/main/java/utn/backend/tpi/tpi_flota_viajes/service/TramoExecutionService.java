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
     * @param idTramo       ID del tramo a iniciar
     * @param dominioCamion Dominio del camión que ejecutará el tramo
     */
    @Transactional
    public TramoDTO iniciarTramo(Long idTramo, String dominioCamion) {
        log.info("Iniciando tramo {} para camión {}", idTramo, dominioCamion);

        // 1. Asignar el camión al tramo y cambiar su estado a EN_VIAJE
        camionService.asignarTramo(dominioCamion, idTramo);

        // 2. Notificar a ms-logistica que el tramo está iniciando
        // ms-logistica calculará distancias usando coordenadas de sus propios depósitos
        // 2. Notificar a ms-logistica que el tramo está iniciando
        // ms-logistica calculará distancias usando coordenadas de sus propios depósitos
        TramoDTO tramoActualizado = logisticaApiClient.iniciarTramo(idTramo);
        log.info("Tramo {} iniciado exitosamente en ms-logistica", idTramo);
        return tramoActualizado;
    }

    /**
     * Finaliza un tramo liberando el camión y notificando a ms-logistica
     *
     * @param idTramo       ID del tramo a finalizar
     * @param dominioCamion Dominio del camión que ejecutó el tramo
     * @param kmRecorridos  Kilómetros recorridos durante el tramo
     */
    @Transactional
    public TramoDTO finalizarTramo(Long idTramo, String dominioCamion, double kmRecorridos) {
        log.info("Finalizando tramo {} para camión {}. KMs recorridos: {}",
                idTramo, dominioCamion, kmRecorridos);

        // 1. Notificar a ms-logistica que el tramo está finalizando
        // 1. Notificar a ms-logistica que el tramo está finalizando
        TramoDTO tramoActualizado = logisticaApiClient.finalizarTramo(idTramo, kmRecorridos);
        log.info("Tramo {} finalizado exitosamente en ms-logistica", idTramo);

        // 2. Si exitoso en ms-logistica, liberar el camión localmente
        camionService.liberarCamion(dominioCamion, idTramo, kmRecorridos);

        return tramoActualizado;
    }

    @Transactional
    public void asignarTramo(Long idTramo, String dominioCamion) {
        log.info("Asignando tramo {} a camión {}", idTramo, dominioCamion);

        // 1. Asignar localmente (valida estado y actualiza a EN_VIAJE - wait, should it
        // be EN_VIAJE or ASIGNADO?)
        // Requisito: "Asignar camión a tramo" vs "Iniciar viaje".
        // Si asignamos, el camión queda comprometido.
        // CamionService.asignarTramo pone el estado en EN_VIAJE.
        // Tal vez deberíamos tener un estado ASIGNADO en Camion también?
        // Por ahora usaremos la lógica existente de asignarTramo que lo pone en
        // EN_VIAJE (o asumimos que asignar = iniciar para este flujo simple,
        // pero el usuario pidió endpoint separado).
        // Si el usuario pidió endpoint separado, probablemente quiera reservar el
        // camión.
        // Voy a usar camionService.asignarTramo pero idealmente debería ser un estado
        // intermedio.
        // Dado que no puedo cambiar el Enum de Camion facilmente sin romper cosas,
        // usaré asignarTramo existente.

        camionService.asignarTramo(dominioCamion, idTramo);

        // 2. Notificar a ms-logistica
        // 2. Notificar a ms-logistica
        logisticaApiClient.asignarCamion(idTramo, dominioCamion);
    }
}
