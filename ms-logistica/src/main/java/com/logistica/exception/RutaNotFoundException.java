package com.logistica.exception;

public class RutaNotFoundException extends LogisticaException {
    public RutaNotFoundException(Long id) {
        super("Ruta no encontrada: " + id);
    }
}