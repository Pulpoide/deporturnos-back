package com.project.deporturnos.entity.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE turno SET deleted = true WHERE id=?")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TurnoState estado;

    @ManyToOne
    @JoinColumn(name = "cancha_id", nullable = false)
    private Cancha cancha;

    @OneToMany(mappedBy = "turno")
    @JsonIgnore
    private Set<Reserva> reservas;

    @Column
    private boolean deleted = Boolean.FALSE;

}
