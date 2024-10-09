package com.project.deporturnos.repository;

import com.project.deporturnos.entity.domain.Cancha;
import com.project.deporturnos.entity.domain.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@EnableJpaRepositories
@Repository
public interface ITurnoRepository extends JpaRepository<Turno, Long>, JpaSpecificationExecutor<Turno> {

    @Query("SELECT t FROM Turno t WHERE t.deleted = false")
    List<Turno> findAllByDeletedFalse();

    boolean existsByCanchaAndFechaAndHoraInicio(Cancha cancha, LocalDate fecha, LocalTime horaInicio);
}
