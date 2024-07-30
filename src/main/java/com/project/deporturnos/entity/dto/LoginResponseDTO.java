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

    private String token;

    private String nombre;
}
