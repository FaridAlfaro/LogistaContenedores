package utn.backend.tpi.tpi_flota_viajes.clients.dto;

import lombok.Data;
import java.time.LocalDateTime;

// DTO espejo de la respuesta de ms-logistica
@Data
public class TramoDTO {
    private Long id;
    private String estado;
    private String dominioCamionRef;
    private String tipo;
    private Double kmEstimados;
    private Double kmRecorridos;
    private Double costoEstimado;
    private Double costoReal;
    private Double tiempoEstimado;
    private Double tiempoReal;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    // ... otros campos que quieras recibir de ms-logistica
}
