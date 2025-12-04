package com.transporte.ms_solicitudes.service;

import com.transporte.ms_solicitudes.api.SolicitudRequestDTO;
import com.transporte.ms_solicitudes.data.ClienteRepository;
import com.transporte.ms_solicitudes.data.ContenedorRepository;
import com.transporte.ms_solicitudes.data.SolicitudRepository;
import com.transporte.ms_solicitudes.model.Cliente;
import com.transporte.ms_solicitudes.model.Contenedor;
import com.transporte.ms_solicitudes.model.EstadoSolicitud;
import com.transporte.ms_solicitudes.model.Localizacion;
import com.transporte.ms_solicitudes.model.Solicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolicitudesService {

    private final SolicitudRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;
    private final KeycloakService keycloakService;

    @Transactional
    public Solicitud crearSolicitud(SolicitudRequestDTO req) {
        // 1. Crear Contenedor
        Contenedor contenedor = Contenedor.builder()
                .tipoCarga(req.tipoCarga())
                .peso(req.peso())
                .refrigerado(req.refrigerado())
                .build();
        contenedorRepository.save(contenedor);

        // 2. Gestionar Cliente (Auto-registro)
        // Buscamos por EMAIL, no por ID
        Optional<Cliente> clienteOpt = clienteRepository.findByEmail(req.email());
        Cliente cliente;

        if (clienteOpt.isPresent()) {
            cliente = clienteOpt.get();
        } else {
            // Es un cliente nuevo: Registrar en Keycloak + DB Local

            // a. Validar password
            String password = (req.password() != null && !req.password().isBlank()) ? req.password() : "default1234";

            // b. Crear en Keycloak (Username = Email)
            keycloakService.crearUsuario(req.email(), password, "CLIENTE");

            // c. Crear en DB Local con ID Generado
            cliente = Cliente.builder()
                    .id(UUID.randomUUID().toString()) // Generamos UUID
                    .email(req.email())
                    .nombre("Cliente " + req.email()) // Nombre temporal
                    .build();

            cliente = clienteRepository.save(cliente);
        }

        // 3. Crear Solicitud
        Solicitud solicitud = Solicitud.builder()
                .idCliente(cliente.getId()) // Usamos el ID interno (UUID), no el email
                .idContenedor(contenedor.getIdContenedor())
                .origen(Localizacion.builder()
                        .lat(req.origen().lat())
                        .lon(req.origen().lon())
                        .build())
                .destino(Localizacion.builder()
                        .lat(req.destino().lat())
                        .lon(req.destino().lon())
                        .build())
                .estado(EstadoSolicitud.CREADA) // Estado Inicial
                .build();

        return solicitudRepository.save(solicitud);
    }

    public Optional<Solicitud> findByNro(String nro) {
        return solicitudRepository.findById(nro);
    }

    public List<Solicitud> findAll() {
        return solicitudRepository.findAll();
    }

    public List<Solicitud> findPendientes() {
        return solicitudRepository.findByEstado(EstadoSolicitud.CREADA);
    }

    @Transactional
    public Optional<Solicitud> aceptarSolicitud(String nro) {
        // Como el estado inicial es PROGRAMADA, este método podría ser redundante
        // o usarse para re-confirmar. Lo mantenemos para compatibilidad.
        return cambiarEstado(nro, EstadoSolicitud.ACEPTADA);
    }

    @Transactional
    public Optional<Solicitud> confirmarEnTransito(String nro) {
        return cambiarEstado(nro, EstadoSolicitud.EN_TRANSITO);
    }

    @Transactional
    public Optional<Solicitud> confirmarEntrega(String nro) {
        return cambiarEstado(nro, EstadoSolicitud.ENTREGADA);
    }

    private Optional<Solicitud> cambiarEstado(String nro, EstadoSolicitud nuevoEstado) {
        return solicitudRepository.findById(nro).map(s -> {
            s.setEstado(nuevoEstado);
            return solicitudRepository.save(s);
        });
    }
}
