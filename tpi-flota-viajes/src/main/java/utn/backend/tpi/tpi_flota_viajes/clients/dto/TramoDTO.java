package utn.backend.tpi.tpi_flota_viajes.clients.dto;

import lombok.Data;

// DTO espejo de la respuesta de ms-logistica
@Data
public class TramoDTO {
    private Long id;
    private String estado;
    private String dominioCamionRef;
    // ... otros campos que quieras recibir de ms-logistica
}
