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
        @Column(name = "nro_solicitud")
        private Long nroSolicitud; // ID Autoincremental (Primary Key)

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

        @Column(name = "id_cliente")
        private Long idCliente;

        @Column(name = "id_contenedor")
        private Long idContenedor;

}
