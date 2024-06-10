package com.project.deporturnos.service;

import com.project.deporturnos.entity.dto.*;

import org.springframework.data.domain.Page;

import com.project.deporturnos.entity.domain.Usuario;

public interface IUsuarioService {

    RegistrationResponseDTO addUsuario(UsuarioDTO usuarioDTO);

    LoginMessageDTO loginUsuario(LoginRequestDTO loginDTO);

    Page<Usuario> getAllUsuarios();

    UsuarioDTO update(Long id, UsuarioRequestUpdateDTO usuarioRequestUpdateDTO);

    void delete(Long id);

}
