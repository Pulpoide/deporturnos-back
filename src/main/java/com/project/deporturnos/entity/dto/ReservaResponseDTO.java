package com.project.deporturnos.entity.dto;

import com.project.deporturnos.entity.domain.ReservaState;
import com.project.deporturnos.entity.domain.Turno;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReservaResponseDTO {

    private Long id;

    private LocalDate fecha;

    private ReservaState estado;

    private UsuarioResponseDTO usuario;

    private Turno turno;

}
