package com.project.deporturnos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import com.project.deporturnos.entity.domain.Usuario;

@EnableJpaRepositories
@Repository
 public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findOneByEmailAndPassword(String nombre, String password);

    Usuario findByEmail(String email);

    boolean existsByEmail(String email);

}
