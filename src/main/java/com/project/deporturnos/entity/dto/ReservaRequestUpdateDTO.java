package com.project.deporturnos.entity.dto;

import com.project.deporturnos.entity.domain.ReservaState;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservaRequestUpdateDTO {

    @Nullable
    private LocalDate fecha;

    @Nullable
    private Long usuarioId;

    @Nullable
    private Long turnoId;

    @Nullable
    private ReservaState estado;
}
