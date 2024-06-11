package com.project.deporturnos.entity.dto;

import com.project.deporturnos.entity.domain.TurnoState;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TurnoRequestDTO {
    @NotNull(message = "La fecha no puede estar vacía")
    private LocalDate fecha;
    @NotNull(message = "La hora de inicio no puede estar vacía")
    private LocalTime horaInicio;
    @NotNull(message = "La hora de fin no puede estar vacía")
    private LocalTime horaFin;
    @Nullable
    private TurnoState estado;
    @NotNull(message = "El ID de la cancha no puede estar vacío")
    private Long canchaId;

}
