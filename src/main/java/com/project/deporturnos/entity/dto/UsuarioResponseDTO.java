package com.project.deporturnos.entity.dto;

import com.project.deporturnos.entity.domain.Rol;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponseDTO {

    private Long id;

    private String nombre;

    private String email;

    private String password;

    private String telefono;

    private Rol rol;

}
