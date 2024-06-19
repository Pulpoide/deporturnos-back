package com.project.deporturnos.repository;

import com.project.deporturnos.entity.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@EnableJpaRepositories
@Repository
public interface IReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioId(Long id);
}
