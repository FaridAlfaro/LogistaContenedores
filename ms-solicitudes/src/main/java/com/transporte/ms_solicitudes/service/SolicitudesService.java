package com.transporte.ms_solicitudes.service;

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
public class SolicitudesService {

    private final SolicitudRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;

    @Transactional
    public Solicitud crearSolicitud(String idCliente, String idContenedor, Double destinoLat, Double destinoLon) {
        // 1. Registrar/Buscar Cliente
        // Asumimos que si no existe, lo creamos con datos dummy o solo el ID por ahora
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseGet(() -> clienteRepository.save(Cliente.builder()
                        .idCliente(idCliente)
                        .nombre("Cliente " + idCliente) // Placeholder
                        .build()));

        // 2. Registrar Contenedor
        // La solicitud incluye la creación del contenedor
        Contenedor contenedor = contenedorRepository.findById(idContenedor)
                .orElseGet(() -> contenedorRepository.save(Contenedor.builder()
                        .idContenedor(idContenedor)
                        .peso(0.0) // Se actualizará después o debería venir en el request
                        .volumen(0.0)
                        .build()));

        // 3. Crear Solicitud
        String nro = UUID.randomUUID().toString().substring(0, 8);
        Solicitud solicitud = Solicitud.builder()
                .nroSolicitud(nro)
                .estado(EstadoSolicitud.BORRADOR)
                .idCliente(cliente.getIdCliente())
                .idContenedor(contenedor.getIdContenedor())
                .destinoLatitud(destinoLat)
                .destinoLongitud(destinoLon)
                .build();

        return solicitudRepository.save(solicitud);
    }

    public Optional<Solicitud> findByNro(String nro) {
        return solicitudRepository.findById(nro);
    }

    public List<Solicitud> findPendientes() {
        return solicitudRepository.findByEstado(EstadoSolicitud.BORRADOR);
    }

    @Transactional
    public Optional<Solicitud> aceptarSolicitud(String nro) {
        return solicitudRepository.findById(nro).map(s -> {
            s.setEstado(EstadoSolicitud.PROGRAMADA); // Cambio de estado a PROGRAMADA (antes ACEPTADA)
            return solicitudRepository.save(s);
        });
    }
}
