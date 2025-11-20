package utn.backend.tpi.tpi_flota_viajes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Camion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String dominio;

    private double capacidadPeso;
    private double capacidadVolumen;
    private double consumoCombustiblePromedio;
    private double costoPorKm;
    private double kmRecorridos;

    @Enumerated(EnumType.STRING)
    private EstadoCamion estado;

    private Long idTramoActual; // ID del tramo en ms-logistica

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id")
    private Transportista transportista;
}