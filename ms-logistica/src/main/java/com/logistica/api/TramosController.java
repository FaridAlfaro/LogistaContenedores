/*
package com.logistica.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistica.model.EstadoTramo;
import com.logistica.model.Tramo;
import com.logistica.repository.TramoRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tramos")
@RequiredArgsConstructor
public class TramosController {
    private final TramoRepository tramoRepository;
    // Este servicio será llamado por ms-flota-viajes

    @GetMapping("/{id}")
    public ResponseEntity<Tramo> getTramoById(@PathVariable Long id) {
        return tramoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/iniciar")
    public ResponseEntity<Tramo> iniciarTramo(@PathVariable Long id) {
        Tramo tramo = tramoRepository.findById(id).orElseThrow();
        tramo.setEstado(EstadoTramo.EN_CURSO);
        return ResponseEntity.ok(tramoRepository.save(tramo));
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<Tramo> finalizarTramo(@PathVariable Long id) {
        Tramo tramo = tramoRepository.findById(id).orElseThrow();
        tramo.setEstado(EstadoTramo.FINALIZADO);
        return ResponseEntity.ok(tramoRepository.save(tramo));
    }
}
*/