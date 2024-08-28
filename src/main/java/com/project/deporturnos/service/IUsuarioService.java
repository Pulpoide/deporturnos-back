package com.project.deporturnos.service;

import com.project.deporturnos.entity.domain.Reserva;
import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface IUsuarioService {

    UsuarioResponseDTO update(Long id, UsuarioRequestUpdateDTO usuarioRequestUpdateDTO);

    List<UsuarioResponseDTO> getAll();

    void delete(Long id);

    UsuarioResponseDTO changeRole(Long id);

    LockUnlockResponseDTO lockUnlock(Long id);

    List<Reserva> findReservationsByUserId(Long id);

    ProfileResUpdateDTO updateProfile(Long id, @Valid ProfileReqUpdateDTO profileReqUpdateDTO);

    Object changePassword(Long id, PasswordChangeRequestDTO passwordChangeRequestDTO);

    Optional<Usuario> findByEmail(String email);

    ResponseEntity<?> resetPassword(Long userId, PasswordResetRequestDTO passwordResetRequestDTO);
}
