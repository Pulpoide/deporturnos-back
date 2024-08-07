package com.project.deporturnos.repository;

import com.project.deporturnos.entity.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@EnableJpaRepositories
@Repository
 public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findOneByEmailAndPassword(String nombre, String password);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.deleted = false")
    List<Usuario> findAllByDeletedFalse();

    Optional<Usuario> findByVerificationCode(String verificationCode);
}
