package utn.backend.tpi.tpi_flota_viajes.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizarTramoRequest {
    /**
     * Dominio del camión que finalizará el tramo
     * Ejemplo: "AY 123 BC"
     */
    @NotBlank(message = "Dominio del camión es obligatorio")
    private String dominioCamion;

    /**
     * Kilómetros recorridos durante el tramo
     * Se acumularán en el registro del camión
     */
    @NotNull(message = "Kilómetros recorridos es obligatorio")
    @Positive(message = "Kilómetros recorridos debe ser mayor a 0")
    private Double kmRecorridos;
}
