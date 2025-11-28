# Resumen de Cambios y Correcciones

Este documento detalla las modificaciones realizadas para solucionar los errores 500 y mejorar la robustez del flujo de Postman, implementando idempotencia y mejor manejo de errores.

## 1. Microservicio de Flota (`tpi-flota-viajes`)

### Idempotencia en Creación de Recursos
Se modificaron los servicios para evitar errores al intentar crear recursos que ya existen (útil cuando se re-ejecuta la colección de Postman).

*   **`TransportistaService.java`**:
    *   Antes de crear un transportista, se verifica si ya existe uno con la misma licencia.
    *   Si existe, se retorna el registro existente en lugar de lanzar un error de base de datos.

*   **`CamionService.java`**:
    *   Antes de crear un camión, se verifica si ya existe uno con el mismo dominio (patente).
    *   Si existe y pertenece al mismo transportista, se retorna el camión existente.
    *   Si existe pero pertenece a otro transportista, se lanza un error `409 Conflict` explícito.

### Manejo de Errores
*   **`GlobalExceptionHandler.java`**:
    *   Se agregó un manejador para `HttpMessageNotReadableException`. Ahora, si el cuerpo del JSON es inválido, se devuelve un `400 Bad Request` claro en lugar de un `500 Internal Server Error`.

## 2. Microservicio de Logística (`ms-logistica`)

### Idempotencia en Creación de Recursos
Similar al microservicio de flota, se asegura que las operaciones de creación sean seguras de repetir.

*   **`DepositoService.java`**:
    *   Se verifica si ya existe un depósito con el mismo nombre. Si es así, se devuelve el existente.

*   **`TarifaService.java`**:
    *   Se verifica si ya existe una tarifa con la misma fecha de vigencia. Si es así, se devuelve la existente.

*   **`RutaService.java`**:
    *   Al planificar una ruta, se verifica si ya existe una ruta asociada al número de solicitud (`nroSolicitud`).
    *   Si existe, se recalculan los totales basados en los tramos guardados y se devuelve la respuesta, evitando duplicados.

### Manejo de Excepciones
*   **Nuevas Excepciones**: Se creó una clase base `NotFoundException` para estandarizar los errores 404.
*   **`GlobalExceptionHandler.java`**: Se actualizó para capturar `NotFoundException` y devolver correctamente el código HTTP `404 Not Found`.

---

## Comandos Docker para Aplicar Cambios

Para compilar los cambios y reiniciar los contenedores con la nueva versión del código, ejecute los siguientes comandos en la raíz del proyecto (donde está el `docker-compose.yml`):

```bash
# 1. Reconstruir y levantar los servicios afectados
docker-compose up -d --build tpi-flota-viajes ms-logistica

# O si prefiere reiniciar todo el entorno:
docker-compose up -d --build
```

### Verificación
Una vez levantados los servicios:
1.  Abra Postman.
2.  Ejecute la colección `TPI_Flowbox` desde el inicio ("0. Autenticación").
3.  El paso "1.4 Crear Camión" (y los demás de creación) debería responder `200 OK` incluso si se ejecuta múltiples veces.
