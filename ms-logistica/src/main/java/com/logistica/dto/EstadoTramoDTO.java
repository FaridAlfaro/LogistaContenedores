package com.logistica.dto;

import com.logistica.model.EstadoTramo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstadoTramoDTO {
    private Long id;
    private EstadoTramo estado;
}