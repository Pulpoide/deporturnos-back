package com.project.deporturnos.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservaRequestUpdateByUserDTO {
    @NotNull(message = "El ID del turno no puede estar vacío")
    private Long turnoId;
}
