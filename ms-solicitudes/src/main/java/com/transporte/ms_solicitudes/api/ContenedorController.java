package com.transporte.ms_solicitudes.api;

import com.transporte.ms_solicitudes.service.ContenedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contenedores")
@RequiredArgsConstructor
public class ContenedorController {

    private final ContenedorService contenedorService;

    @GetMapping("/{id}/estado")
    public ResponseEntity<EstadoContenedorDTO> estadoContenedor(@PathVariable String id) {
        return ResponseEntity.ok(contenedorService.obtenerEstadoContenedor(id));
    }
}

