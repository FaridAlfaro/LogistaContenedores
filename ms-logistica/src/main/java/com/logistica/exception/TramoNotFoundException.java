package com.logistica.exception;

public class TramoNotFoundException extends LogisticaException {
    public TramoNotFoundException(Long id) {
        super("Tramo no encontrado: " + id);
    }
}