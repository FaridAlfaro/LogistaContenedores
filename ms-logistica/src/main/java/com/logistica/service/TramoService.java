package com.logistica.service;

import com.logistica.model.*;
import com.logistica.repository.TramoRepository;
import com.logistica.repository.RutaRepository;
import com.logistica.client.OsrmClient2;
import com.logistica.client.FlotaApiClient;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TramoService {

    private final TramoRepository tramoRepository;
    private final RutaRepository rutaRepository;
    private final OsrmClient2 osrmClient;
    private final RabbitTemplate rabbitTemplate;
    private final FlotaApiClient flotaApiClient;

    /**
     * Marca un tramo como INICIADO (llamado por MS Flota)
     * Publica evento a RabbitMQ para MS Solicitudes
     * @return El tramo actualizado
     */
    @Transactional
    public Tramo marcarTramoIniciado(Long idTramo) {
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
        
        return tramo;
    }

    /**
     * Marca un tramo como FINALIZADO (llamado por MS Flota)
     * Calcula costos y tiempos reales
     * Publica evento a RabbitMQ para MS Solicitudes
     * @return El tramo actualizado
     */
    @Transactional
    public Tramo marcarTramoFinalizado(Long idTramo, double kmRecorridos) {
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
        
        // Verificar si todos los tramos de la ruta están finalizados
        verificarYActualizarEstadoRuta(ruta);
        
        return tramo;
    }

    /**
     * Verifica si todos los tramos de una ruta están finalizados
     * y publica evento para actualizar el estado de la solicitud
     */
    private void verificarYActualizarEstadoRuta(Ruta ruta) {
        List<Tramo> tramos = tramoRepository.findByRutaId(ruta.getId());
        boolean todosFinalizados = tramos.stream()
                .allMatch(t -> t.getEstado() == EstadoTramo.FINALIZADO);
        
        if (todosFinalizados && !tramos.isEmpty()) {
            // Calcular costos totales
            double costoTotalReal = tramos.stream()
                    .mapToDouble(Tramo::getCostoReal)
                    .sum();
            
            double tiempoTotalReal = tramos.stream()
                    .mapToDouble(Tramo::getTiempoReal)
                    .sum();
            
            log.info("Todos los tramos de la ruta {} están finalizados. Costo total: ${}, Tiempo total: {}s",
                    ruta.getId(), costoTotalReal, tiempoTotalReal);
            
            // Publicar evento para actualizar solicitud a ENTREGADA
            TramoFinalizado eventoFinal = new TramoFinalizado(
                    null, // idTramo null indica que es el evento final de toda la ruta
                    ruta.getNroSolicitudRef(),
                    0, // kmRecorridos no aplica
                    costoTotalReal,
                    tiempoTotalReal,
                    LocalDateTime.now(),
                    "ENTREGADA");
            
            rabbitTemplate.convertAndSend("solicitudes.exchange", "ruta.completada", eventoFinal);
            log.info("Evento de ruta completada publicado para solicitud: {}", ruta.getNroSolicitudRef());
        }
    }

    /**
     * Calcula el costo real del tramo según requerimientos:
     * - Costo por km del camión específico
     * - Costo de combustible (km * consumo del camión * precio del litro)
     * - Estadía en depósito (días * costo diario del depósito)
     */
    private double calcularCostoReal(Tramo tramo, double kmRecorridos) {
        Tarifa tarifa = tramo.getTarifa();
        double costoTotal = 0.0;

        // 1. Costo por km del camión específico
        if (tramo.getDominioCamionRef() != null && !tramo.getDominioCamionRef().isEmpty()) {
            try {
                FlotaApiClient.CamionInfo camion = flotaApiClient.obtenerCamionPorDominio(tramo.getDominioCamionRef());
                double costoPorKmCamion = kmRecorridos * camion.getCostoPorKm();
                costoTotal += costoPorKmCamion;
                log.debug("Costo por km del camión {}: ${}", tramo.getDominioCamionRef(), costoPorKmCamion);

                // 2. Costo de combustible
                // Litros consumidos = km * (consumo del camión en L/km)
                double litrosConsumidos = kmRecorridos * (camion.getConsumoCombustiblePromedio() / 100.0); // convertir de L/100km a L/km
                double costoCombustible = litrosConsumidos * tarifa.getCostoLitroCombustible();
                costoTotal += costoCombustible;
                log.debug("Costo de combustible: {} L * ${}/L = ${}", litrosConsumidos, tarifa.getCostoLitroCombustible(), costoCombustible);
            } catch (Exception e) {
                log.warn("No se pudo obtener información del camión {}, usando tarifa base: {}", 
                        tramo.getDominioCamionRef(), e.getMessage());
                // Fallback: usar tarifa base si no se puede obtener el camión
                costoTotal += kmRecorridos * tarifa.getValorKMBase();
            }
        } else {
            // Si no hay camión asignado, usar tarifa base
            costoTotal += kmRecorridos * tarifa.getValorKMBase();
        }

        // 3. Estadía en depósito (calculada entre fin de este tramo y inicio del siguiente)
        // Nota: La estadía se calcula cuando el siguiente tramo inicia desde este depósito
        // Por ahora, si el tramo termina en un depósito, calculamos estadía mínima de 1 día
        // La estadía completa se calculará cuando el siguiente tramo inicie
        if (tramo.getDepositoDestino() != null && tramo.getFechaHoraFin() != null) {
            // Buscar el siguiente tramo que sale de este depósito
            List<Tramo> tramosRuta = tramoRepository.findByRutaId(tramo.getRuta().getId());
            Tramo siguienteTramo = tramosRuta.stream()
                    .filter(t -> t.getDepositoOrigen() != null && 
                            t.getDepositoOrigen().getId().equals(tramo.getDepositoDestino().getId()) &&
                            t.getFechaHoraInicio() != null)
                    .findFirst()
                    .orElse(null);
            
            if (siguienteTramo != null && siguienteTramo.getFechaHoraInicio() != null) {
                // Calcular días entre fin de este tramo e inicio del siguiente
                long diasEstadia = ChronoUnit.DAYS.between(tramo.getFechaHoraFin(), siguienteTramo.getFechaHoraInicio());
                if (diasEstadia > 0) {
                    double costoEstadia = diasEstadia * tramo.getDepositoDestino().getCostoEstadiaDiario();
                    costoTotal += costoEstadia;
                    log.debug("Costo de estadía en depósito {}: {} días * ${}/día = ${}", 
                            tramo.getDepositoDestino().getNombre(), diasEstadia, 
                            tramo.getDepositoDestino().getCostoEstadiaDiario(), costoEstadia);
                }
            } else if (tramo.getFechaHoraFin() != null) {
                // Si no hay siguiente tramo aún, asumir estadía mínima de 1 día
                // Esto se ajustará cuando el siguiente tramo inicie
                double costoEstadia = 1 * tramo.getDepositoDestino().getCostoEstadiaDiario();
                costoTotal += costoEstadia;
                log.debug("Costo de estadía estimada en depósito {}: 1 día * ${}/día = ${}", 
                        tramo.getDepositoDestino().getNombre(), 
                        tramo.getDepositoDestino().getCostoEstadiaDiario(), costoEstadia);
            }
        }

        log.info("Costo real calculado para tramo {}: ${}", tramo.getId(), costoTotal);
        return costoTotal;
    }

    /**
     * Calcula el tiempo real del tramo
     */
    private double calcularTiempoReal(Tramo tramo) {
        if (tramo.getFechaHoraInicio() == null) {
            return 0;
        }
        LocalDateTime fin = LocalDateTime.now();

        return java.time.temporal.ChronoUnit.SECONDS.between(
                tramo.getFechaHoraInicio(),
                fin);
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

    public Optional<Tramo> obtenerTramo(Long id) {
        return tramoRepository.findById(id);
    }

    public List<Tramo> listarTramos() {
        return tramoRepository.findAll();
    }
}