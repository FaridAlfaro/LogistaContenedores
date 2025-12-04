package utn.backend.tpi.tpi_flota_viajes.exception;

import org.springframework.dao.DataIntegrityViolationException;
import utn.backend.tpi.tpi_flota_viajes.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// IMPORTS NECESARIOS PARA KEYCLOAK
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.NotAuthorizedException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex, WebRequest request) {
                log.warn("NotFoundException: {}", ex.getMessage());
                return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
        }

        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex, WebRequest request) {
                log.warn("ConflictException: {}", ex.getMessage());
                return buildResponse(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request);
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
                log.warn("BadRequestException: {}", ex.getMessage());
                return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request);
        }

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex, WebRequest request) {
                log.warn("ForbiddenException: {}", ex.getMessage());
                return buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), request);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
                        WebRequest request) {
                log.warn("Error de validación");
                List<String> detalles = new ArrayList<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> detalles.add(error.getField() + ": " + error.getDefaultMessage()));

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .mensaje("Error de validación en los datos enviados")
                                .codigo("VALIDATION_ERROR")
                                .status(HttpStatus.BAD_REQUEST.value())
                                .timestamp(LocalDateTime.now())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .detalles(detalles)
                                .build();
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // --- NUEVO: MANEJO DE ERRORES DE KEYCLOAK (JAX-RS) ---
        @ExceptionHandler({ ProcessingException.class, WebApplicationException.class })
        public ResponseEntity<ErrorResponse> handleKeycloakException(RuntimeException ex, WebRequest request) {
                log.error("Error comunicación Keycloak: {}", ex.getMessage());

                HttpStatus status = HttpStatus.BAD_GATEWAY;
                String mensaje = "Error de comunicación con el servidor de identidad.";

                if (ex instanceof NotAuthorizedException) {
                        status = HttpStatus.UNAUTHORIZED;
                        mensaje = "Error de configuración: Backend no autorizado en Keycloak (Revisar Client Secret).";
                } else if (ex instanceof WebApplicationException webEx) {
                        int code = webEx.getResponse().getStatus();
                        if (code >= 400 && code < 500) {
                                status = HttpStatus.BAD_REQUEST; // O CONFLICT según el caso
                                mensaje = "Keycloak rechazó la petición: " + webEx.getMessage();
                        }
                }

                return buildResponse(status, "IDENTITY_PROVIDER_ERROR", mensaje, request);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                        WebRequest request) {
                log.error("Integrity Violation: {}", ex.getMessage());
                return buildResponse(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                                "Error de datos duplicados o referencia inválida.", request);
        }

        @ExceptionHandler({ ResourceAccessException.class, RestClientException.class })
        public ResponseEntity<ErrorResponse> handleExternalServiceException(Exception ex, WebRequest request) {
                log.error("External Service Error: {}", ex.getMessage());
                return buildResponse(HttpStatus.BAD_GATEWAY, "EXTERNAL_SERVICE_ERROR",
                                "Servicio externo no disponible.", request);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
                log.error("Error no controlado: {}", ex.getMessage(), ex);
                return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                                "Error interno del servidor.", request);
        }

        private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message,
                        WebRequest request) {
                ErrorResponse response = ErrorResponse.builder()
                                .mensaje(message)
                                .codigo(code)
                                .status(status.value())
                                .timestamp(LocalDateTime.now())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();
                return new ResponseEntity<>(response, status);
        }
}