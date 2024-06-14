package com.project.deporturnos.service;

import com.project.deporturnos.entity.dto.*;

import org.springframework.data.domain.Page;

import com.project.deporturnos.entity.domain.Usuario;

public interface IUsuarioService {

    Page<Usuario> getAllUsuarios();

    UsuarioResponseDTO update(Long id, UsuarioRequestUpdateDTO usuarioRequestUpdateDTO);

    void delete(Long id);

    UsuarioResponseDTO changeRole(Long id);

    LockUnlockResponseDTO lockUnlock(Long id);
}
