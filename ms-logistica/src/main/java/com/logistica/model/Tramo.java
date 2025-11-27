package com.logistica.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class Tramo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ruta_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Ruta ruta;

    @ManyToOne
    @JoinColumn(name = "deposito_origen_id")
    private Deposito depositoOrigen;

    @ManyToOne
    @JoinColumn(name = "deposito_destino_id")
    private Deposito depositoDestino;

    @ManyToOne
    @JoinColumn(name = "tarifa_id", nullable = false)
    private Tarifa tarifa;

    private String tipo; // Origen-Deposito, Deposito-Deposito, Deposito-Destino, Origen-Destino

    @Enumerated(EnumType.STRING)
    private EstadoTramo estado; // ESTIMADO, ASIGNADO, INICIADO, FINALIZADO

    private double kmEstimados;
    private double kmRecorridos;

    private double costoEstimado;
    private double costoReal;

    private double tiempoEstimado; // en segundos
    private double tiempoReal; // en segundos

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    // Referencia a MS Flota
    private String dominioCamionRef;
}