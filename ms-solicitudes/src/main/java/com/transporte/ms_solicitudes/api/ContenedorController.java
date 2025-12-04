package com.transporte.ms_solicitudes.api;

import com.transporte.ms_solicitudes.model.Contenedor;
import com.transporte.ms_solicitudes.service.ContenedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contenedores")
@RequiredArgsConstructor
public class ContenedorController {

    private final ContenedorService contenedorService;

    @PostMapping
    public ResponseEntity<Contenedor> crear(@RequestBody Contenedor contenedor) {
        Contenedor nuevo = contenedorService.crearContenedor(contenedor);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }
}
