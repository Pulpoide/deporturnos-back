package com.project.deporturnos.entity.dto;

import com.project.deporturnos.entity.domain.Deporte;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CanchaResponseDTO {

    private Long id;

    private String nombre;

    private String tipo;

    private BigDecimal precioHora;

    private boolean disponibilidad;

    private String descripcion;

    private Deporte deporte;

    private int turnosDisponibles;
}
