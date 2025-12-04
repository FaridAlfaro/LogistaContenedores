package com.transporte.ms_solicitudes.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String mensaje;
    private String codigo;
    private int status;
    private LocalDateTime timestamp;
    private String path;
    private List<String> detalles;
}
