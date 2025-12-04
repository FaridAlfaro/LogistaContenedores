package com.transporte.ms_solicitudes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clientes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @Column(name = "id_cliente")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // UUID Generado automáticamente (No es el email)

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String email; // El email es único y sirve para login/busqueda
}