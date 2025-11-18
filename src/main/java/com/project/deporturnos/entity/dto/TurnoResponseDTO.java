package com.project.deporturnos.entity.dto;

import com.project.deporturnos.entity.domain.TurnoState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TurnoResponseDTO {

    private Long id;

    private LocalDate fecha;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private TurnoState estado;

    private CanchaSimpleDTO cancha;
}
