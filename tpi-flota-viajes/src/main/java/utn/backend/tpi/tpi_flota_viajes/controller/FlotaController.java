package utn.backend.tpi.tpi_flota_viajes.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import utn.backend.tpi.tpi_flota_viajes.clients.dto.TramoDTO;
import utn.backend.tpi.tpi_flota_viajes.dto.request.*;
import utn.backend.tpi.tpi_flota_viajes.dto.response.CamionResponse;
import utn.backend.tpi.tpi_flota_viajes.dto.response.ListaCamionesDisponiblesResponse;
import utn.backend.tpi.tpi_flota_viajes.dto.response.TransportistaResponse;
import utn.backend.tpi.tpi_flota_viajes.service.CamionService;
import utn.backend.tpi.tpi_flota_viajes.service.TramoExecutionService;
import utn.backend.tpi.tpi_flota_viajes.service.TransportistaService;

import java.util.List;

@RestController
@RequestMapping("/api/flota") // Ruta base para todo este MS
@RequiredArgsConstructor
public class FlotaController {

    private final CamionService camionService;
    private final TransportistaService transportistaService;
    private final TramoExecutionService tramoExecutionService;

    // --- Endpoints de FLOTA (OPERADOR) ---

    @PostMapping("/camiones")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<CamionResponse> addCamion(@Valid @RequestBody CrearCamionRequest crearCamionRequest) {
        CamionResponse response = camionService.addCamion(crearCamionRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/camiones/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<CamionResponse> getCamionById(@PathVariable Long id) {
        return ResponseEntity.ok(camionService.obtenerPorId(id));
    }

    @GetMapping("/camiones/dominio/{dominio}")
    public ResponseEntity<CamionResponse> getCamionByDominio(@PathVariable String dominio) {
        return ResponseEntity.ok(camionService.obtenerPorDominio(dominio));
    }

    @GetMapping("/camiones")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<CamionResponse>> getAllCamiones() {
        return ResponseEntity.ok(camionService.listarTodos());
    }

    @PostMapping("/camiones/disponibles")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<ListaCamionesDisponiblesResponse> getCamionesDisponibles(
            @Valid @RequestBody ObtenerDisponiblesRequest request) {
        ListaCamionesDisponiblesResponse response = camionService.getCamionesDisponibles(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/transportistas")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TransportistaResponse> addTransportista(
            @Valid @RequestBody CrearTransportistaRequest crearTransportistaRequest) {
        TransportistaResponse response = transportistaService.crear(crearTransportistaRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/transportistas/{id}/camiones")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<CamionResponse>> getCamionesPorTransportista(@PathVariable Long id) {
        return ResponseEntity.ok(camionService.obtenerPorTransportista(id));
    }

    // --- Endpoints de VIAJES (TRANSPORTISTA) ---

    @PostMapping("/tramos/{idTramo}/iniciar")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    public ResponseEntity<TramoDTO> iniciarTramo(@PathVariable Long idTramo, @RequestBody IniciarTramoRequest request) {
        @Valid
        TramoDTO tramoActualizado = tramoExecutionService.iniciarTramo(idTramo, request.getDominioCamion());
        return ResponseEntity.ok(tramoActualizado);
    }

    @PostMapping("/tramos/{idTramo}/finalizar")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    public ResponseEntity<TramoDTO> finalizarTramo(@PathVariable Long idTramo,
            @Valid @RequestBody FinalizarTramoRequest request) {
        TramoDTO tramoActualizado = tramoExecutionService.finalizarTramo(idTramo, request.getDominioCamion(),
                request.getKmRecorridos());
        return ResponseEntity.ok(tramoActualizado);
    }

    @PostMapping("/tramos/{idTramo}/asignar")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Void> asignarTramo(@PathVariable Long idTramo, @RequestParam String dominio) {
        tramoExecutionService.asignarTramo(idTramo, dominio);
        return ResponseEntity.ok().build();
    }
}