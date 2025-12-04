package utn.backend.tpi.tpi_flota_viajes.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramoResponse {
    private Long idTramo;
    private String estado; // ESTIMADO, ASIGNADO, INICIADO, FINALIZADO
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;
    private Double kmRecorridos;
    private String dominioCamion;
    private Long camionId;
}
