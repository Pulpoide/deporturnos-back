package com.project.deporturnos.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CanchaSimpleDTO {

    private Long id;
    private String nombre;
    private String tipo;
    private int precioHora; 
    // Los campos 'descripcion', 'disponibilidad' y 'deleted' se omiten aquí.
}
