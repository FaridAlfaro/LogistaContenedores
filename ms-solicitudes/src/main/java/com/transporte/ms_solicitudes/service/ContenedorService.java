package com.transporte.ms_solicitudes.service;

import com.transporte.ms_solicitudes.api.EstadoContenedorDTO;
import com.transporte.ms_solicitudes.data.ClienteRepository;
import com.transporte.ms_solicitudes.data.ContenedorRepository;
import com.transporte.ms_solicitudes.data.SolicitudRepository;
import com.transporte.ms_solicitudes.model.Cliente;
import com.transporte.ms_solicitudes.model.Contenedor;
import com.transporte.ms_solicitudes.model.EstadoSolicitud;
import com.transporte.ms_solicitudes.model.Solicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContenedorService {

    private final SolicitudRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;

    public EstadoContenedorDTO obtenerEstadoContenedor(String idContenedor) {

        Contenedor c = contenedorRepository.findById(idContenedor)
                .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado: " + idContenedor));

        return new EstadoContenedorDTO(c.getIdContenedor(), c.getEstado());
    }

}
