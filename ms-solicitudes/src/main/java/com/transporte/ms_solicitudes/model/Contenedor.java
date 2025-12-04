package com.transporte.ms_solicitudes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contenedor_seq")
    @SequenceGenerator(name = "contenedor_seq", sequenceName = "contenedor_seq", allocationSize = 1)
    private Long idContenedor; // Identificación única
    private String tipoCarga;
    private Double peso;
    private Double volumen;
    private boolean refrigerado;
}
