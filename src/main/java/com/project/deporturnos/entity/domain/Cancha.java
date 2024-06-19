package com.project.deporturnos.entity.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Getter
@Setter
@Entity
public class Cancha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String nombre;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private double precioHora;

    @Column(nullable = false)
    private boolean disponibilidad;

    @Column
    private String descripcion;

    @Column
    private Deporte deporte;

    @OneToMany(mappedBy = "cancha")
    @JsonIgnore
    private Set<Turno> turnos;
}

