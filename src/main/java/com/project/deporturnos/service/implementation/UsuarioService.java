package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.Reserva;
import com.project.deporturnos.entity.domain.Rol;
import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.LockUnlockResponseDTO;
import com.project.deporturnos.entity.dto.UsuarioRequestUpdateDTO;
import com.project.deporturnos.entity.dto.UsuarioResponseDTO;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.exception.UserAlreadyExistsException;
import com.project.deporturnos.repository.IReservaRepository;
import com.project.deporturnos.repository.IUsuarioRepository;
import com.project.deporturnos.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UsuarioService implements IUsuarioService, UserDetailsService {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IReservaRepository  reservaRepository;

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

            // Validación de email
            String regex = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,}$";
            java.util.regex.Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(usuarioRequestUpdateDTO.getEmail());
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Correo electrónico no válido.");
            }

            Optional<Usuario> usuarioOptional2 = usuarioRepository.findByEmail(usuarioRequestUpdateDTO.getEmail());
            if(usuarioOptional2.isPresent()){
                throw new UserAlreadyExistsException("Ya existe un usuario con ese email.");
            }

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

    @Override
    public List<Reserva> findReservationsByUserId(Long id) {
        // Get current user from SecurityContext
        Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(currentUser.getRol().equals(Rol.CLIENTE)) {
            if (!Objects.equals(id, currentUser.getId())) {
                throw new InsufficientAuthenticationException("No autorizado.");
            }
        }
        return reservaRepository.findByUsuarioId(id);
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);

        if (usuario.isPresent()) {
            return usuario.get();
        }

        throw new UsernameNotFoundException("User Not Found");
    }
}
