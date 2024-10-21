package com.project.deporturnos.entity.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;
import java.util.Set;


@Getter
@Setter
@Entity
@SQLDelete(sql = "UPDATE cancha SET deleted = true WHERE id=?")
public class Cancha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String nombre;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private BigDecimal precioHora;

    @Column(nullable = false)
    private boolean disponibilidad;

    @Column
    private String descripcion;

    @Column
    private Deporte deporte;

    @OneToMany(mappedBy = "cancha")
    @JsonIgnore
    private Set<Turno> turnos;

    @Column
    private boolean deleted = Boolean.FALSE;
}

