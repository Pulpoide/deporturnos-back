package com.project.deporturnos.entity.dto;

import com.project.deporturnos.entity.domain.ReservaState;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservaRequestDTO {
    @NotNull(message = "La fecha no puede estar vacía")
    private LocalDate fecha;
    @Nullable
    private Long usuarioId;
    @NotNull(message = "El ID del turno no puede estar vacío")
    private Long turnoId;
    @Nullable
    private ReservaState estado;
}
