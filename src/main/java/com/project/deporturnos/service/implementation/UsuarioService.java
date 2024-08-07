package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.*;
import com.project.deporturnos.entity.dto.LockUnlockResponseDTO;
import com.project.deporturnos.entity.dto.UsuarioRequestUpdateDTO;
import com.project.deporturnos.entity.dto.UsuarioResponseDTO;
import com.project.deporturnos.exception.InvalidEmailException;
import com.project.deporturnos.exception.InvalidPasswordException;
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

import java.util.ArrayList;
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
    public List<UsuarioResponseDTO> getAll(){
        List<Usuario> usuarios = usuarioRepository.findAllByDeletedFalse();

        if(usuarios.isEmpty()){
            throw new ResourceNotFoundException("No se encontraron usuarios para listar");
        }

        List<UsuarioResponseDTO> usuarioResponseDTOS = new ArrayList<>();
        for(Usuario usuario : usuarios){
            usuarioResponseDTOS.add(mapper.convertValue(usuario, UsuarioResponseDTO.class));
        }

        return usuarioResponseDTOS;
    }


    @Override
    public void delete(Long id){
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if(usuarioOptional.isPresent()){
            Usuario usuario = usuarioOptional.get();
            usuario.setDeleted(true);
            usuario.setActivada(false);

            // Marcamos todas las reservas del usuario como eliminadas
            for(Reserva reserva : usuario.getReservas()){
                reserva.setDeleted(true);

                // Ponemos el turno asociado a la reserva en estado DISPONIBLE si todas sus reservas estan eliminadas
                Turno turno = reserva.getTurno();
                boolean allReservasDeleted = turno.getReservas().stream().allMatch(Reserva::isDeleted);
                if (allReservasDeleted) {
                    turno.setEstado(TurnoState.DISPONIBLE);
                }
            }
            usuarioRepository.save(usuario);
        }else{
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
    }


    @Override
    public UsuarioResponseDTO update(Long id, UsuarioRequestUpdateDTO usuarioRequestUpdateDTO){
        return getUsuarioResponseDTO(id, usuarioRequestUpdateDTO);
    }

    private UsuarioResponseDTO getUsuarioResponseDTO(Long id, UsuarioRequestUpdateDTO usuarioRequestUpdateDTO) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if(usuarioOptional.isEmpty()){
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOptional.get();

        if(usuarioRequestUpdateDTO.getNombre() != null){
            usuario.setNombre(usuarioRequestUpdateDTO.getNombre());
        }

        if(usuarioRequestUpdateDTO.getEmail() != null){

            // Validación de email
            String regex = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,}$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(usuarioRequestUpdateDTO.getEmail());
            if (!matcher.matches()) {
                throw new InvalidEmailException("Correo electrónico no válido");
            }

            Optional<Usuario> usuarioOptional2 = usuarioRepository.findByEmail(usuarioRequestUpdateDTO.getEmail());
            if(usuarioOptional2.isPresent()){
                throw new UserAlreadyExistsException("Ya existe un usuario con ese email");
            }

            usuario.setEmail(usuarioRequestUpdateDTO.getEmail());
        }

        if(usuarioRequestUpdateDTO.getPassword() != null){

            // Validación de password
            String regexPass = "^(?=\\w*\\d)(?=\\w*[a-z])\\S{8,16}$";
            Pattern patternPass = Pattern.compile(regexPass);
            Matcher matcherPass = patternPass.matcher(usuarioRequestUpdateDTO.getPassword());
            if (!matcherPass.matches()) {
                throw new InvalidPasswordException("Contraseña no válida");
            }

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
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        if(id == 1){
            throw new IllegalArgumentException("No es posible cambiarle el roll al administrador supremo");
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
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOptional.get();

        usuario.setActivada(!usuarioOptional.get().isActivada());

        Usuario usuarioSaved = usuarioRepository.save(usuario);
        return mapper.convertValue(usuarioSaved, LockUnlockResponseDTO.class);
    }

    @Override
    public List<Reserva> findReservationsByUserId(Long id) {
        // Get current user from SecurityContext
        Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(currentUser.getRol().equals(Rol.CLIENTE)) {
            if (!Objects.equals(id, currentUser.getId())) {
                throw new InsufficientAuthenticationException("No autorizado");
            }
        }
        return reservaRepository.findByUsuarioId(id);
    }

    @Override
    public UsuarioResponseDTO updateProfile(Long id, UsuarioRequestUpdateDTO usuarioRequestUpdateDTO) {

       Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(currentUser.getRol().equals(Rol.CLIENTE)) {
            if (!id.equals(currentUser.getId())) {
                throw new InsufficientAuthenticationException("No autorizado");
            }
        }

        return getUsuarioResponseDTO(id, usuarioRequestUpdateDTO);

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
