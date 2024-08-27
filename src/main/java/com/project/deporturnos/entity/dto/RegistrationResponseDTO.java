package com.project.deporturnos.entity.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationResponseDTO {
    private Long id;

    private String nombre;

    private String email;

    private String telefono;

    private String token;

}
