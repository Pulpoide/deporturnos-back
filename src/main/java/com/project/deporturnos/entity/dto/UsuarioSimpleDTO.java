package com.project.deporturnos.entity.dto;

import com.project.deporturnos.entity.domain.Rol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioSimpleDTO {

    private Long id;

    private String nombre;

    private String email;

    private String telefono;

    private Rol rol;

    private boolean activada;

    private boolean notificaciones;
}
