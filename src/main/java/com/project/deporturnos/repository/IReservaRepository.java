package com.project.deporturnos.repository;

import com.project.deporturnos.entity.domain.Reserva;
import com.project.deporturnos.entity.domain.ReservaState;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@EnableJpaRepositories
@Repository
public interface IReservaRepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {

    List<Reserva> findByUsuarioIdAndEstadoNotAndDeletedFalse(Long id, ReservaState state);

    List<Reserva> findByUsuarioIdAndDeletedFalse(Long id);

    List<Reserva> findByEstado(ReservaState state);

    @Query("SELECT r FROM Reserva r WHERE r.deleted = false")
    Page<Reserva> findAllByDeletedFalse(Pageable pageable);
}
