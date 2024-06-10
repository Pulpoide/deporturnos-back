package com.project.deporturnos.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponseDTO {

    private Long id;

    private String nombre;

    private String email;

    private String telefono;

    private boolean cuentaActivada;

}
