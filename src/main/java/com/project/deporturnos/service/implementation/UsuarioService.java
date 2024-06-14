package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.Rol;
import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.LockUnlockResponseDTO;
import com.project.deporturnos.entity.dto.UsuarioRequestUpdateDTO;
import com.project.deporturnos.entity.dto.UsuarioResponseDTO;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.repository.IUsuarioRepository;
import com.project.deporturnos.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService implements IUsuarioService {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper mapper;


    @Override
    public Page<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll(Pageable.unpaged());
    }


    @Override
    public void delete(Long id){
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if(usuarioOptional.isPresent()){
            usuarioRepository.deleteById(id);
        }else{
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }
    }


    @Override
    public UsuarioResponseDTO update(Long id, UsuarioRequestUpdateDTO usuarioRequestUpdateDTO){
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if(usuarioOptional.isEmpty()){
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOptional.get();

        if(usuarioRequestUpdateDTO.getNombre() != null){
            usuario.setNombre(usuarioRequestUpdateDTO.getNombre());
        }

        if(usuarioRequestUpdateDTO.getEmail() != null){
            usuario.setEmail(usuarioRequestUpdateDTO.getEmail());
        }

        if(usuarioRequestUpdateDTO.getPassword() != null){
            usuario.setPassword(passwordEncoder.encode(usuarioRequestUpdateDTO.getPassword()));
        }

        if(usuarioRequestUpdateDTO.getTelefono() != null){
            usuario.setTelefono(usuarioRequestUpdateDTO.getTelefono());
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return mapper.convertValue(usuarioGuardado, UsuarioResponseDTO.class);
    }


    @Override
    public UsuarioResponseDTO changeRole(Long id) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);

        if(usuarioOptional.isEmpty()){
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOptional.get();

        if(usuarioOptional.get().getRol().equals(Rol.ADMIN)){
            usuario.setRol(Rol.CLIENTE);
        }else{
            usuario.setRol(Rol.ADMIN);
        }

        Usuario usuarioSaved = usuarioRepository.save(usuario);
        return mapper.convertValue(usuarioSaved, UsuarioResponseDTO.class);
    }

    @Override
    public LockUnlockResponseDTO lockUnlock(Long id) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);

        if(usuarioOptional.isEmpty()){
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOptional.get();

        usuario.setCuentaActivada(!usuarioOptional.get().isCuentaActivada());

        Usuario usuarioSaved = usuarioRepository.save(usuario);
        return mapper.convertValue(usuarioSaved, LockUnlockResponseDTO.class);
    }


}
