package com.project.deporturnos.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String token;

    private long expiresIn;

    private String email;

    private Long id;

    private String nombre;

    private String telefono;

    private Boolean notificaciones;
}
