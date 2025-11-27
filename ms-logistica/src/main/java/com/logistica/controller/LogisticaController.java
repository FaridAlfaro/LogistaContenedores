package com.logistica.controller;

import com.logistica.dto.request.PlanificarRutaRequest;
import com.logistica.dto.response.CalculoResponse;
import com.logistica.dto.response.DistanciaResponse;
import com.logistica.model.*;
import com.logistica.service.RutaService;
import com.logistica.service.DepositoService;
import com.logistica.service.TarifaService;
import com.logistica.service.TramoService;
import com.logistica.dto.response.RutaPlanningResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class LogisticaController {

    private final RutaService rutaService;
    private final DepositoService depositoService;
    private final TarifaService tarifaService;
    private final TramoService tramoService;

    /**
     * Planifica una ruta calculando tramos, distancias y costos estimados
     * POST /api/v1/rutas/planificar
     */
    @PostMapping("/rutas/planificar")
    public ResponseEntity<RutaPlanningResponse> planificarRuta(@RequestBody PlanificarRutaRequest request) {
        log.info("Planificando ruta para solicitud: {}", request.getNroSolicitud());

        try {
            List<Deposito> depositos = request.getIdDepositos() != null && !request.getIdDepositos().isEmpty()
                    ? request.getIdDepositos().stream()
                            .map(depositoService::obtenerDeposito)
                            .toList()
                    : List.of();

            Tarifa tarifa = tarifaService.obtenerTarifa(request.getIdTarifa());

            RutaPlanningResponse response = rutaService.planificarRuta(
                    request.getNroSolicitud(),
                    depositos,
                    request.getLatOrigen(),
                    request.getLonOrigen(),
                    request.getLatDestino(),
                    request.getLonDestino(),
                    tarifa);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error planificando ruta: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/rutas/calcular")
    public ResponseEntity<CalculoResponse> calcularRuta(
            @RequestParam Double latOrigen,
            @RequestParam Double lonOrigen,
            @RequestParam Double latDestino,
            @RequestParam Double lonDestino,
            @RequestParam(required = false) List<Long> idDepositos,
            @RequestParam Long idTarifa) {

        log.info("Calculando ruta con {} depósitos", idDepositos != null ? idDepositos.size() : 0);

        try {
            Tarifa tarifa = tarifaService.obtenerTarifa(idTarifa);

            List<Deposito> depositos = idDepositos != null && !idDepositos.isEmpty()
                    ? idDepositos.stream()
                            .map(depositoService::obtenerDeposito)
                            .toList()
                    : List.of();

            DistanciaResponse distanciaResponse = rutaService.calcularDistancia(
                    latOrigen, lonOrigen, latDestino, lonDestino, depositos);

            double costoEstimado = distanciaResponse.getDistanciaKm() * tarifa.getValorKMBase();

            CalculoResponse response = new CalculoResponse(
                    distanciaResponse.getDistanciaKm(),
                    distanciaResponse.getTiempoSegundos(),
                    costoEstimado);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculando ruta: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Crea un nuevo depósito
     * POST /api/v1/depositos
     */
    @PostMapping("/depositos")
    public ResponseEntity<Deposito> crearDeposito(@RequestBody Deposito deposito) {
        log.info("Creando depósito: {}", deposito.getNombre());
        Deposito depositoCreado = depositoService.crearDeposito(deposito);
        return ResponseEntity.status(HttpStatus.CREATED).body(depositoCreado);
    }

    /**
     * Crea una nueva tarifa
     * POST /api/v1/tarifas
     */
    @PostMapping("/tarifas")
    public ResponseEntity<Tarifa> crearTarifa(@RequestBody Tarifa tarifa) {
        log.info("Creando tarifa vigente desde: {}", tarifa.getFechaVigencia());
        Tarifa tarifaCreada = tarifaService.crearTarifa(tarifa);
        return ResponseEntity.status(HttpStatus.CREATED).body(tarifaCreada);
    }

    /**
     * INTERNO: Recibe notificación síncrona de MS Flota - Tramo iniciado
     * PUT /api/v1/tramos/{id}/iniciar
     */
    @PutMapping("/tramos/{id}/iniciar")
    public ResponseEntity<Void> iniciarTramo(@PathVariable Long id) {
        log.info("Tramo {} iniciado (notificación de MS Flota)", id);
        tramoService.marcarTramoIniciado(id);
        return ResponseEntity.ok().build();
    }

    /**
     * INTERNO: Recibe notificación síncrona de MS Flota - Tramo finalizado
     * PUT /api/v1/tramos/{id}/finalizar
     */
    @PutMapping("/tramos/{id}/finalizar")
    public ResponseEntity<Void> finalizarTramo(@PathVariable Long id,
            @RequestParam double kmRecorridos) {
        log.info("Tramo {} finalizado con {} km (notificación de MS Flota)", id, kmRecorridos);
        tramoService.marcarTramoFinalizado(id, kmRecorridos);
        return ResponseEntity.ok().build();
    }

    /**
     * Asigna un camión a un tramo (llamado por MS Flota o Operador)
     * POST /api/v1/tramos/{id}/asignar
     */
    @PostMapping("/tramos/{id}/asignar")
    public ResponseEntity<Void> asignarCamion(@PathVariable Long id, @RequestParam String dominio) {
        log.info("Asignando camión {} al tramo {}", dominio, id);
        tramoService.asignarCamion(id, dominio);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene un tramo por su ID
     * GET /api/v1/tramos/{id}
     */
    @GetMapping("/tramos/{id}")
    public ResponseEntity<Tramo> getTramoById(@PathVariable Long id) {
        return tramoService.obtenerTramo(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Consulta contenedores pendientes de entrega y su ubicación
     * GET /api/v1/contenedores/pendientes
     */
    @GetMapping("/contenedores/pendientes")
    public ResponseEntity<List<Tramo>> obtenerContenedoresPendientes() {
        return ResponseEntity.ok(tramoService.obtenerTramosPendientes());
    }
    
}