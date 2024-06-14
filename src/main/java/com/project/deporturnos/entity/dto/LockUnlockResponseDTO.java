package com.project.deporturnos.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LockUnlockResponseDTO {

    private Long id;

    private String email;

    private boolean cuentaActivada;
}
