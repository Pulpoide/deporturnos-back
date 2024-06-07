package com.project.deporturnos.service;

import com.project.deporturnos.entity.dto.LoginResponseDTO;

import org.springframework.data.domain.Page;

import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.LoginMessageDTO;
import com.project.deporturnos.entity.dto.UsuarioDTO;

public interface IUsuarioService {

    String addUsuario(UsuarioDTO usuarioDTO);

    LoginMessageDTO loginUsuario(LoginResponseDTO loginDTO);

    Page<Usuario> getAllUsuarios();
    //Page<Usuario> getAllUsuarios(Integer page, Integer size, Boolean enablePagination);
}
