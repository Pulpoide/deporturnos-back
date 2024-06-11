package com.project.deporturnos.entity.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CanchaRequestUpdateDTO {
    @Nullable
    private String nombre;
    @Nullable
    private String tipo;

    private double precioHora;

    private boolean disponibilidad;
    @Nullable
    private String descripcion;
}
