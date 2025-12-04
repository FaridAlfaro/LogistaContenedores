package com.transporte.ms_solicitudes.model;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "solicitudes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "solicitud_seq")
    @SequenceGenerator(name = "solicitud_seq", sequenceName = "solicitud_seq", allocationSize = 1)
    private Long id; // este es el ID real en la DB

    private String nroSolicitud;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;

    private Double costoEstimado;
    private Integer tiempoEstimado;
    private Double costoFinal;
    private Integer tiempoReal;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "origen_lat")),
            @AttributeOverride(name = "lon", column = @Column(name = "origen_lon"))
    })
    private Localizacion origen;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "destino_lat")),
            @AttributeOverride(name = "lon", column = @Column(name = "destino_lon"))
    })
    private Localizacion destino;

    private String idCliente;
    private String idContenedor;
    private String idRutaRef;

    @PrePersist
    public void generarNroSolicitud() {
        // Ejemplo: prefijo + ID con padding
        this.nroSolicitud = "SOL-" + String.format("%08d", this.id);
    }
}