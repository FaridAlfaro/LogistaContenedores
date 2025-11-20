package utn.backend.tpi.tpi_flota_viajes.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IniciarTramoRequest {
    /**
     * Dominio del camión que iniciará el tramo
     * Ejemplo: "AY 123 BC"
     * Se utiliza para buscar el camión en BD y cambiar su estado a EN_VIAJE
     */
    @NotBlank(message = "Dominio del camión es obligatorio")
    private String dominioCamion;
}
