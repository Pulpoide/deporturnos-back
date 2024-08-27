package com.project.deporturnos.service;

import com.project.deporturnos.entity.domain.Reserva;
import com.project.deporturnos.entity.dto.*;
import jakarta.validation.Valid;

import java.util.List;

public interface IUsuarioService {

    UsuarioResponseDTO update(Long id, UsuarioRequestUpdateDTO usuarioRequestUpdateDTO);

    List<UsuarioResponseDTO> getAll();

    void delete(Long id);

    UsuarioResponseDTO changeRole(Long id);

    LockUnlockResponseDTO lockUnlock(Long id);

    List<Reserva> findReservationsByUserId(Long id);

    ProfileResUpdateDTO updateProfile(Long id, @Valid ProfileReqUpdateDTO profileReqUpdateDTO);

    Object changePassword(Long id, PasswordChangeRequestDTO passwordChangeRequestDTO);
}
