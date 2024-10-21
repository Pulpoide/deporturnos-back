package com.project.deporturnos.entity.dto;

import com.project.deporturnos.entity.domain.Deporte;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CanchaRequestUpdateDTO {
    @Nullable
    private String nombre;
    @Nullable
    private String tipo;

    private BigDecimal precioHora;

    private boolean disponibilidad;
    @Nullable
    private String descripcion;
    @Nullable
    private Deporte deporte;

}
