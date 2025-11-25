package com.logistica.service;

import com.logistica.model.*;
import com.logistica.repository.TramoRepository;
import com.logistica.repository.RutaRepository;
import com.logistica.client.OsrmClient2;
import com.logistica.event.TramoIniciado;
import com.logistica.event.TramoFinalizado;
import com.logistica.exception.TramoNotFoundException;
import com.logistica.exception.RutaNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TramoService {

    private final TramoRepository tramoRepository;
    private final RutaRepository rutaRepository;
    private final OsrmClient2 osrmClient;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Marca un tramo como INICIADO (llamado por MS Flota)
     * Publica evento a RabbitMQ para MS Solicitudes
     */
    @Transactional
    public void marcarTramoIniciado(Long idTramo) {
        log.info("Marcando tramo {} como EN CURSO", idTramo);

        Tramo tramo = tramoRepository.findById(idTramo)
                .orElseThrow(() -> new TramoNotFoundException(idTramo));

        Ruta ruta = tramo.getRuta();
        if (ruta == null) {
            throw new RutaNotFoundException(null);
        }

        tramo.setEstado(EstadoTramo.EN_CURSO);
        tramo.setFechaHoraInicio(LocalDateTime.now());
        tramoRepository.save(tramo);

        log.info("Tramo {} ahora está EN CURSO, publicando evento", idTramo);

        // Publica evento a RabbitMQ para MS Solicitudes
        TramoIniciado evento = new TramoIniciado(
                idTramo,
                ruta.getNroSolicitudRef(),
                LocalDateTime.now(),
                "INICIADO");

        rabbitTemplate.convertAndSend("solicitudes.exchange", "tramo.iniciado", evento);
        log.info("Evento TramoIniciado publicado para solicitud: {}", ruta.getNroSolicitudRef());
    }

    /**
     * Marca un tramo como FINALIZADO (llamado por MS Flota)
     * Calcula costos y tiempos reales
     * Publica evento a RabbitMQ para MS Solicitudes
     */
    @Transactional
    public void marcarTramoFinalizado(Long idTramo, double kmRecorridos) {
        log.info("Marcando tramo {} como FINALIZADO con {} km recorridos", idTramo, kmRecorridos);

        Tramo tramo = tramoRepository.findById(idTramo)
                .orElseThrow(() -> new TramoNotFoundException(idTramo));

        Ruta ruta = tramo.getRuta();
        if (ruta == null) {
            throw new RutaNotFoundException(null);
        }

        // Calcular costo real y tiempo real
        double costoReal = calcularCostoReal(tramo, kmRecorridos);
        double tiempoReal = calcularTiempoReal(tramo);

        tramo.setEstado(EstadoTramo.FINALIZADO);
        tramo.setKmRecorridos(kmRecorridos);
        tramo.setCostoReal(costoReal);
        tramo.setTiempoReal(tiempoReal);
        tramo.setFechaHoraFin(LocalDateTime.now());
        tramoRepository.save(tramo);

        log.info("Tramo {} finalizado. Costo real: ${}, Tiempo real: {}s",
                idTramo, costoReal, tiempoReal);

        // Publica evento a RabbitMQ para MS Solicitudes
        TramoFinalizado evento = new TramoFinalizado(
                idTramo,
                ruta.getNroSolicitudRef(),
                kmRecorridos,
                costoReal,
                tiempoReal,
                LocalDateTime.now(),
                "FINALIZADO");

        rabbitTemplate.convertAndSend("solicitudes.exchange", "tramo.finalizado", evento);
        log.info("Evento TramoFinalizado publicado para solicitud: {}", ruta.getNroSolicitudRef());
    }

    /**
     * Calcula el costo real del tramo
     */
    private double calcularCostoReal(Tramo tramo, double kmRecorridos) {
        Tarifa tarifa = tramo.getTarifa();

        // Costo por km
        double costoPorKm = kmRecorridos * tarifa.getValorKMBase();

        // TODO: Agregar costo de combustible, estadía en depósito, etc.

        return costoPorKm;
    }

    /**
     * Calcula el tiempo real del tramo
     */
    private double calcularTiempoReal(Tramo tramo) {
        if (tramo.getFechaHoraInicio() == null || tramo.getFechaHoraFin() == null) {
            return 0;
        }

        return java.time.temporal.ChronoUnit.SECONDS.between(
                tramo.getFechaHoraInicio(),
                tramo.getFechaHoraFin());
    }

    @Transactional
    public void asignarCamion(Long idTramo, String dominio) {
        Tramo tramo = tramoRepository.findById(idTramo)
                .orElseThrow(() -> new TramoNotFoundException(idTramo));

        tramo.setDominioCamionRef(dominio);
        tramo.setEstado(EstadoTramo.ASIGNADO);
        tramoRepository.save(tramo);
    }

    public List<Tramo> obtenerTramosPendientes() {
        // Retorna tramos que están ASIGNADO, INICIADO o ESTIMADO (no FINALIZADO)
        return tramoRepository.findByEstadoNot(EstadoTramo.FINALIZADO);
    }
}