package com.project.deporturnos.entity.dto;

import com.project.deporturnos.entity.domain.TurnoState;
import jakarta.annotation.Nullable;
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
public class TurnoRequestUpdateDTO {

    @Nullable
    private LocalDate fecha;
    @Nullable
    private LocalTime horaInicio;
    @Nullable
    private LocalTime horaFin;
    @Nullable
    private TurnoState estado;
    @Nullable
    private Long canchaId;

}
