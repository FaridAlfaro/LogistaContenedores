package com.transporte.ms_solicitudes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contenedores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contenedor {
    @Id
    private String idContenedor; // Identificación única

    @Enumerated(EnumType.STRING)
    private EstadoContenedor estado;
    private Double peso;
    private Double volumen;
    private boolean refrigerado;
}
