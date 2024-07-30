package com.project.deporturnos.entity.dto;

import com.project.deporturnos.entity.domain.Deporte;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CanchaRequestDTO {
    @Nullable
    private String nombre;
    @NotNull
    private String tipo;
    @NotNull
    private double precioHora;
    @NotNull
    private boolean disponibilidad;
    @Nullable
    private String descripcion;
    @Nullable
    private Deporte deporte;
}
