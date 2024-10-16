package com.project.deporturnos.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CargaMasivaTurnosDTO {
    @NotNull
    private LocalDate fechaDesde;
    @NotNull
    private LocalDate fechaHasta;
    @NotNull
    private LocalTime horaDesde;
    @NotNull
    private LocalTime horaHasta;
    @NotNull
    private int duracionEnMinutos;
    @NotNull
    private Long canchaId;
}
