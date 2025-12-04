package com.transporte.ms_solicitudes.service;

import com.transporte.ms_solicitudes.data.ContenedorRepository;
import com.transporte.ms_solicitudes.model.Contenedor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContenedorService {

    private final ContenedorRepository contenedorRepository;

    @Transactional
    public Contenedor crearContenedor(Contenedor contenedor) {
        return contenedorRepository.save(contenedor);
    }
}
