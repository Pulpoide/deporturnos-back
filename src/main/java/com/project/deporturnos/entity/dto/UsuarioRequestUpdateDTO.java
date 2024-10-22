package com.project.deporturnos.entity.dto;

import io.micrometer.common.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioRequestUpdateDTO {

    @Nullable
    private String nombre;
    @Nullable
    private String email;
    @Nullable
    private String password;
    @Nullable
    private String telefono;
    @Nullable
    private Boolean notificaciones;
}
