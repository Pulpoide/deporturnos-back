package com.project.deporturnos.entity.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LoginResponseDTO {

    private Long id;

    private String nombre;

    private String email;

    private String token;

    private long expiresIn;
}
