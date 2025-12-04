package com.transporte.ms_solicitudes.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Localizacion {
    private Double lat;
    private Double lon;
}
