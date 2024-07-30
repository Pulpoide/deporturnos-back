package com.project.deporturnos.repository;

import com.project.deporturnos.entity.domain.Cancha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@EnableJpaRepositories
@Repository
public interface ICanchaRepository extends JpaRepository<Cancha, Long> {
    @Query("SELECT c FROM Cancha c WHERE c.deleted = false")
    List<Cancha> findAllByDeletedFalse();
}
