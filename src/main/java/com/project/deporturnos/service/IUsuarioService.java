package com.project.deporturnos.service;

import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.*;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface IUsuarioService {

    UsuarioResponseDTO update(Long id, UsuarioRequestUpdateDTO usuarioRequestUpdateDTO);

    Page<UsuarioSimpleDTO> getPaginatedData(Pageable pageable, String search);

    void delete(Long id);

    UsuarioResponseDTO changeRole(Long id);

    LockUnlockResponseDTO lockUnlock(Long id);

    Page<ReservaResponseDTO> findReservations(Long id, String estado, Pageable pageable);

    ProfileResUpdateDTO updateProfile(Long id, @Valid ProfileReqUpdateDTO profileReqUpdateDTO);

    Object changePassword(Long id, PasswordChangeRequestDTO passwordChangeRequestDTO);

    Optional<Usuario> findByEmail(String email);

    ResponseEntity<?> resetPassword(Long userId, PasswordResetRequestDTO passwordResetRequestDTO);
}
