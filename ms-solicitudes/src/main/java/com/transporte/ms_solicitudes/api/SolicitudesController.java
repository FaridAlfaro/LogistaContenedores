package com.transporte.ms_solicitudes.api;

import com.transporte.ms_solicitudes.model.Solicitud;
import com.transporte.ms_solicitudes.service.SolicitudesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
public class SolicitudesController {

    private final SolicitudesService service;

    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> crear(@Valid @RequestBody SolicitudRequestDTO req) {
        // La validación @Valid maneja los nulos en el DTO
        if (req.origen() == null || req.destino() == null) {
            throw new IllegalArgumentException("Origen y Destino no pueden ser null");
        }

        // Pasamos el objeto 'req' completo al servicio
        Solicitud nueva = service.crearSolicitud(req);

        SolicitudResponseDTO dto = new SolicitudResponseDTO(
                nueva.getNroSolicitud(),
                nueva.getEstado().name());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{nro}")
    public ResponseEntity<Solicitud> porNro(@PathVariable String nro) {
        return service.findByNro(nro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Solicitud> listarTodas() {
        return service.findAll();
    }

    @GetMapping("/pendientes")
    public List<Solicitud> pendientes() {
        return service.findPendientes();
    }

    @PutMapping("/{nro}/aceptar")
    public ResponseEntity<Solicitud> aceptar(@PathVariable String nro) {
        return service.aceptarSolicitud(nro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{nro}/en-transito")
    public ResponseEntity<Solicitud> enTransito(@PathVariable String nro) {
        return service.confirmarEnTransito(nro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{nro}/entregada")
    public ResponseEntity<Solicitud> entregada(@PathVariable String nro) {
        return service.confirmarEntrega(nro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}