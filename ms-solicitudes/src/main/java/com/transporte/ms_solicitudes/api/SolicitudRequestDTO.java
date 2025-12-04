package com.transporte.ms_solicitudes.api;

import jakarta.validation.constraints.NotNull;

public record SolicitudRequestDTO(
                @NotNull(message = "El email es obligatorio para el registro") String email, // <--- CAMBIO: Ahora
                                                                                             // pedimos explícitamente
                                                                                             // el email
                String password, // Requerido si el usuario no existe

                @NotNull(message = "El tipo de carga es obligatorio") String tipoCarga,
                @NotNull(message = "El peso es obligatorio") @jakarta.validation.constraints.Positive Double peso,
                boolean refrigerado, // Default false si no viene

                @NotNull Localizacion origen,

                @NotNull Localizacion destino) {

        public record Localizacion(Double lat, Double lon) {
        }
}

record SolicitudResponseDTO(
                Long nroSolicitud,
                String estado) {
}

record TrackingDTO(
                String estado,
                String ubicacionActual,
                String proximoTramo,
                String ETA) {
}
