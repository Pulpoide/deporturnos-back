package com.project.deporturnos.entity.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaDTO(
    Long id,
    LocalDate fechaCreacion,
    LocalDate fechaTurno,
    LocalTime horaInicio,
    LocalTime horaFin,
    String estado
) {}
