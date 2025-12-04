package com.transporte.ms_solicitudes.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.NotAuthorizedException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler({ ProcessingException.class, WebApplicationException.class })
    public ResponseEntity<ErrorResponse> handleKeycloakException(
            RuntimeException ex,
            WebRequest request) {

        log.error("Error de comunicación con Keycloak: {}", ex.getMessage());

        HttpStatus status = HttpStatus.BAD_GATEWAY; // Por defecto asumimos fallo de conexión
        String mensaje = "Error de comunicación con el servidor de identidad (Keycloak).";

        if (ex instanceof NotAuthorizedException) {
            status = HttpStatus.UNAUTHORIZED;
            mensaje = "Error de credenciales del backend para acceder a Keycloak (Check client-secret).";
        } else if (ex instanceof WebApplicationException webEx) {
            // Si Keycloak respondió con un error específico
            if (webEx.getResponse().getStatus() >= 400 && webEx.getResponse().getStatus() < 500) {
                status = HttpStatus.BAD_REQUEST;
                mensaje = "Error en la petición a Keycloak: " + webEx.getMessage();
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .mensaje(mensaje)
                .codigo("IDENTITY_PROVIDER_ERROR")
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .detalles(List.of(ex.getMessage())) // Útil para debug
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, String>> handleNullPointerException(NullPointerException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error: Null Pointer Exception");
        error.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        ex.printStackTrace(); // <--- IMPORTANTE PARA DEPURAR
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
