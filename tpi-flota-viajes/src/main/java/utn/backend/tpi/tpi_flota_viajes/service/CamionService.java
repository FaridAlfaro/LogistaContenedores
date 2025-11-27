package utn.backend.tpi.tpi_flota_viajes.service;

import org.springframework.dao.DataIntegrityViolationException;
import utn.backend.tpi.tpi_flota_viajes.dto.request.CrearCamionRequest;
import utn.backend.tpi.tpi_flota_viajes.dto.request.ObtenerDisponiblesRequest;
import utn.backend.tpi.tpi_flota_viajes.dto.response.CamionResponse;
import utn.backend.tpi.tpi_flota_viajes.dto.response.ListaCamionesDisponiblesResponse;
import utn.backend.tpi.tpi_flota_viajes.dto.response.CamionDisponibleItemResponse;
import utn.backend.tpi.tpi_flota_viajes.dto.mapper.CamionMapper;
import utn.backend.tpi.tpi_flota_viajes.model.Camion;
import utn.backend.tpi.tpi_flota_viajes.model.EstadoCamion;
import utn.backend.tpi.tpi_flota_viajes.model.Transportista;
import utn.backend.tpi.tpi_flota_viajes.repository.CamionRepository;
import utn.backend.tpi.tpi_flota_viajes.repository.TransportistaRepository;
import utn.backend.tpi.tpi_flota_viajes.exception.NotFoundException;
import utn.backend.tpi.tpi_flota_viajes.exception.ConflictException;
import utn.backend.tpi.tpi_flota_viajes.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CamionService {
    private final CamionRepository camionRepository;
    private final TransportistaRepository transportistaRepository;

    /**
     * Crear un nuevo camión
     * Validar: transportista existe, capacidades positivas
     * Maneja race condition en validación de dominio único
     */
    @Transactional
    public CamionResponse addCamion(CrearCamionRequest dto) {
        log.info("Iniciando creación de camión con dominio: {}", dto.getDominio());

        // VALIDACIÓN 1: Transportista existe
        Transportista transportista = transportistaRepository.findById(dto.getIdTransportista())
                .orElseThrow(() -> {
                    log.warn("Transportista no encontrado con ID: {}", dto.getIdTransportista());
                    return new NotFoundException("Transportista con ID " + dto.getIdTransportista() + " no encontrado");
                });

        // VALIDACIÓN 1.5: Transportista debe estar ACTIVO
        if (transportista.getActivo() == null || !transportista.getActivo()) {
            log.warn("Intento de crear camión para transportista inactivo: {}", dto.getIdTransportista());
            throw new ConflictException("No se puede crear camión para un transportista inactivo");
        }

        // VALIDACIÓN 2: Capacidades positivas
        if (dto.getCapacidadPeso() <= 0 || dto.getCapacidadVolumen() <= 0) {
            log.warn("Intento de crear camión con capacidades inválidas. Peso: {}, Volumen: {}",
                    dto.getCapacidadPeso(), dto.getCapacidadVolumen());
            throw new BadRequestException("Las capacidades deben ser mayores a 0");
        }

        if (dto.getConsumoCombustiblePromedio() <= 0 || dto.getCostoPorKm() <= 0) {
            log.warn("Intento de crear camión con consumo o costo inválido. Consumo: {}, Costo: {}",
                    dto.getConsumoCombustiblePromedio(), dto.getCostoPorKm());
            throw new BadRequestException("Consumo y costo deben ser mayores a 0");
        }

        try {
            // CREAR CAMIÓN
            Camion camion = Camion.builder()
                    .dominio(dto.getDominio().toUpperCase()) // Normalizar a mayúsculas
                    .capacidadPeso(dto.getCapacidadPeso())
                    .capacidadVolumen(dto.getCapacidadVolumen())
                    .consumoCombustiblePromedio(dto.getConsumoCombustiblePromedio())
                    .costoPorKm(dto.getCostoPorKm())
                    .estado(EstadoCamion.DISPONIBLE) // Nuevo camión siempre comienza DISPONIBLE
                    .transportista(transportista)
                    .idTramoActual(null) // Sin tramo asignado al inicio
                    .kmRecorridos(0.0) // Inicia en 0
                    .build();

            Camion guardado = camionRepository.save(camion);
            log.info("Camión creado exitosamente con ID: {}, dominio: {}, transportista: {}",
                    guardado.getId(), guardado.getDominio(), transportista.getNombre());

            return CamionMapper.toResponse(guardado);

        } catch (DataIntegrityViolationException e) {
            // Captura race condition: dos peticiones simultáneas con mismo dominio
            log.error("Error de integridad: dominio duplicado en BD - {}. Transportista: {}",
                    dto.getDominio(), dto.getIdTransportista(), e);
            throw new ConflictException("El dominio " + dto.getDominio() + " ya está registrado");
        }
    }

    /**
     * Obtener camión por ID
     */
    public CamionResponse obtenerPorId(Long id) {
        log.debug("Buscando camión con ID: {}", id);

        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Camión no encontrado con ID: {}", id);
                    return new NotFoundException("Camión con ID " + id + " no encontrado");
                });

        return CamionMapper.toResponse(camion);
    }

    /**
     * Obtener camión por dominio
     */
    public CamionResponse obtenerPorDominio(String dominio) {
        log.debug("Buscando camión con dominio: {}", dominio);

        Camion camion = camionRepository.findByDominio(dominio.toUpperCase())
                .orElseThrow(() -> {
                    log.warn("Camión no encontrado con dominio: {}", dominio);
                    return new NotFoundException("Camión con dominio " + dominio + " no encontrado");
                });

        return CamionMapper.toResponse(camion);
    }

    /**
     * Obtener camiones disponibles con capacidad suficiente
     * REGLA DE NEGOCIO: "Un camión no puede transportar contenedores que superen su
     * peso o volumen máximo"
     */
    public ListaCamionesDisponiblesResponse getCamionesDisponibles(ObtenerDisponiblesRequest request) {

        Double capacidadPesoRequerida = request.getCapacidadPesoRequerida();
        Double capacidadVolumenRequerida = request.getCapacidadVolumenRequerida();

        log.info("Buscando camiones: peso={} kg, volumen={} m³",
                capacidadPesoRequerida, capacidadVolumenRequerida);

        // VALIDACIÓN
        if (capacidadPesoRequerida <= 0 || capacidadVolumenRequerida <= 0) {
            log.warn("Capacidades inválidas requeridas. Peso: {}, Volumen: {}",
                    capacidadPesoRequerida, capacidadVolumenRequerida);
            throw new BadRequestException("Capacidades deben ser mayores a 0");
        }

        // BUSCAR: BD hace todo el filtro
        List<Camion> camionesDisponibles = camionRepository.findDisponiblesConCapacidad(
                EstadoCamion.DISPONIBLE,
                capacidadPesoRequerida,
                capacidadVolumenRequerida).stream()
                .limit(50)
                .collect(Collectors.toList());

        log.info("Se encontraron {} camiones disponibles", camionesDisponibles.size());

        List<CamionDisponibleItemResponse> items = camionesDisponibles.stream()
                .map(CamionMapper::toDisponibleItemResponse)
                .collect(Collectors.toList());

        return ListaCamionesDisponiblesResponse.builder()
                .camiones(items)
                .total((long) camionesDisponibles.size())
                .build();
    }

    /**
     * Cambiar estado de un camión
     * Valida transiciones de estado válidas
     * Uso interno: cuando inicia/finaliza un tramo o entra en mantenimiento
     */
    @Transactional
    public void cambiarEstado(Long camionId, EstadoCamion nuevoEstado) {
        log.debug("Cambiando estado del camión {} a: {}", camionId, nuevoEstado);

        Camion camion = camionRepository.findById(camionId)
                .orElseThrow(() -> {
                    log.warn("Camión no encontrado para cambiar estado. ID: {}", camionId);
                    return new NotFoundException("Camión con ID " + camionId + " no encontrado");
                });

        EstadoCamion estadoActual = camion.getEstado();

        // VALIDACIÓN: Transiciones permitidas
        if (!esTransicionValida(estadoActual, nuevoEstado)) {
            log.warn("Transición de estado no permitida. Camión: {}, De: {}, A: {}",
                    camionId, estadoActual, nuevoEstado);
            throw new ConflictException(
                    String.format("No se puede cambiar estado de %s a %s", estadoActual, nuevoEstado));
        }

        camion.setEstado(nuevoEstado);
        camionRepository.save(camion);

        log.info("Estado del camión {} actualizado de {} a {}",
                camionId, estadoActual, nuevoEstado);
    }

    /**
     * Valida si la transición de estado es permitida
     * Transiciones válidas:
     * - DISPONIBLE ↔ EN_USO
     * - DISPONIBLE ↔ MANTENIMIENTO
     * - EN_USO → DISPONIBLE (solo cuando se libera)
     */
    private boolean esTransicionValida(EstadoCamion estadoActual, EstadoCamion nuevoEstado) {

        // No cambiar a sí mismo
        if (estadoActual == nuevoEstado) {
            return true; // Es válido no cambiar
        }

        // Transiciones permitidas
        return switch (estadoActual) {
            case DISPONIBLE -> nuevoEstado == EstadoCamion.EN_VIAJE || nuevoEstado == EstadoCamion.MANTENIMIENTO;
            case EN_VIAJE -> nuevoEstado == EstadoCamion.DISPONIBLE; // Solo a DISPONIBLE
            case MANTENIMIENTO -> nuevoEstado == EstadoCamion.DISPONIBLE; // Solo a DISPONIBLE
        };
    }

    /**
     * Asignar tramo a un camión
     * Se ejecuta cuando se asigna un camión a un tramo
     */
    @Transactional
    public void asignarTramo(String dominio, Long tramoId) {
        log.debug("Asignando tramo {} al camión {}", tramoId, dominio);

        // VALIDACIÓN: tramoId debe ser válido
        if (tramoId == null || tramoId <= 0) {
            log.warn("Intento de asignar tramoId inválido. Camión: {}, Tramo: {}",
                    dominio, tramoId);
            throw new BadRequestException("ID de tramo inválido");
        }

        Camion camion = camionRepository.findByDominio(dominio)
                .orElseThrow(() -> new NotFoundException("Camión con dominio " + dominio + " no encontrado"));

        // VALIDACIÓN: Camión debe estar disponible
        if (camion.getEstado() != EstadoCamion.DISPONIBLE) {
            log.warn("Intento de asignar tramo a camión no disponible. Camión: {}, Estado: {}, Tramo: {}",
                    dominio, camion.getEstado(), tramoId);
            throw new ConflictException("El camión no está disponible");
        }

        camion.setIdTramoActual(tramoId); // Guardar ID del tramo
        camion.setEstado(EstadoCamion.EN_VIAJE); // Cambiar a EN_VIAJE
        camionRepository.save(camion);

        log.info("Tramo {} asignado al camión {}. Transportista: {}",
                tramoId, dominio, camion.getTransportista().getNombre());
    }

    /**
     * Liberar camión después de finalizar un tramo
     */
    @Transactional
    public void liberarCamion(String dominio, Long tramoId, double kmRecorridos) {
        log.debug("Liberando camión: {}", dominio);

        Camion camion = camionRepository.findByDominio(dominio)
                .orElseThrow(() -> {
                    log.warn("Camión no encontrado para liberar. Dominio: {}", dominio);
                    return new NotFoundException("Camión con dominio " + dominio + " no encontrado");
                });

        // VALIDACIÓN: Debe estar EN_VIAJE
        if (camion.getEstado() != EstadoCamion.EN_VIAJE) {
            log.warn("Intento de liberar camión que no está en viaje. Dominio: {}, Estado: {}",
                    dominio, camion.getEstado());
            throw new ConflictException("El camión no está en viaje, no puede ser liberado");
        }

        // VALIDACIÓN: Debe estar en el tramo correcto
        if (camion.getIdTramoActual() == null || !camion.getIdTramoActual().equals(tramoId)) {
            log.warn(
                    "Intento de liberar camión de un tramo incorrecto. Dominio: {}, Tramo Camion: {}, Tramo Reportado: {}",
                    dominio, camion.getIdTramoActual(), tramoId);
            throw new ConflictException("El camion no estaba asignado al tramo " + tramoId);
        }

        try {
            camion.setIdTramoActual(null);
            camion.setEstado(EstadoCamion.DISPONIBLE);
            camion.setKmRecorridos(camion.getKmRecorridos() + kmRecorridos); // Acumular KMs
            camionRepository.save(camion);

            log.info("Camión {} liberado y disponible. Transportista: {}",
                    dominio, camion.getTransportista().getNombre());

        } catch (DataIntegrityViolationException e) {
            log.error("Error al liberar camión {}. Estado inconsistente.", dominio, e);
            throw new ConflictException("Error al liberar el camión. Contacte al administrador.");
        }
    }

    /**
     * Obtener todos los camiones de un transportista
     */
    @Transactional(readOnly = true)
    public List<CamionResponse> obtenerPorTransportista(Long transportistaId) {
        log.debug("Buscando camiones del transportista: {}", transportistaId);

        // VALIDACIÓN: Transportista debe existir
        Transportista transportista = transportistaRepository.findById(transportistaId)
                .orElseThrow(() -> {
                    log.warn("Transportista no encontrado para obtener camiones. ID: {}", transportistaId);
                    return new NotFoundException("Transportista con ID " + transportistaId + " no encontrado");
                });

        List<Camion> camiones = camionRepository.findByTransportistaId(transportistaId);
        log.info("Se encontraron {} camiones para transportista: {} ({})",
                camiones.size(), transportistaId, transportista.getNombre());

        return camiones.stream()
                .map(CamionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Contar camiones disponibles (para estadísticas)
     */
    public Long contarDisponibles() {
        long cantidad = camionRepository.countByEstado(EstadoCamion.DISPONIBLE);
        log.debug("Cantidad de camiones disponibles: {}", cantidad);
        return cantidad;
    }

    @Transactional(readOnly = true)
    public List<CamionResponse> listarTodos() {
        return camionRepository.findAll().stream()
                .map(CamionMapper::toResponse)
                .collect(Collectors.toList());
    }
}