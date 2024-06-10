package com.project.deporturnos.repository;

import com.project.deporturnos.entity.domain.Cancha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@EnableJpaRepositories
@Repository
public interface ICanchaRepository extends JpaRepository<Cancha, Long> {

}
