package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.*;
import com.project.deporturnos.entity.dto.*;
import com.project.deporturnos.exception.InvalidEmailException;
import com.project.deporturnos.exception.InvalidPasswordException;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.exception.UserAlreadyExistsException;
import com.project.deporturnos.repository.IReservaRepository;
import com.project.deporturnos.repository.IUsuarioRepository;
import com.project.deporturnos.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

        Usuario usuarioSaved = usuarioRepository.save(usuario);
        return mapper.convertValue(usuarioSaved, UsuarioResponseDTO.class);
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
    public ProfileResUpdateDTO updateProfile(Long id, ProfileReqUpdateDTO profileReqUpdateDTO) {

       Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(currentUser.getRol().equals(Rol.CLIENTE)) {
            if (!id.equals(currentUser.getId())) {
                throw new InsufficientAuthenticationException("No autorizado");
            }
        }

        if(profileReqUpdateDTO.getNombre() != null){
            currentUser.setNombre(profileReqUpdateDTO.getNombre());
        }
        if(profileReqUpdateDTO.getTelefono() != null){
            currentUser.setTelefono(profileReqUpdateDTO.getTelefono());
        }

        Usuario usuarioSaved = usuarioRepository.save(currentUser);

        return mapper.convertValue(usuarioSaved, ProfileResUpdateDTO.class);

    }

    @Override
    public ResponseEntity<?> changePassword(Long id, PasswordChangeRequestDTO passwordChangeRequestDTO) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);

        if(usuarioOptional.isEmpty()){
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOptional.get();

        // Verificación de la contraseña actual
        if(!passwordEncoder.matches(passwordChangeRequestDTO.getOldPassword(), usuario.getPassword())){
            throw new InvalidPasswordException("Contraseña actual incorrecta");
        }

        // Validación y actualización de la nueva contraseña
        validateAndUpdatePassword(mapper.convertValue(passwordChangeRequestDTO, PasswordResetRequestDTO.class), usuario);

        return ResponseEntity.ok(new ApiResponse(true, "Contraseña cambiada exitosamente"));
    }

    private void validateAndUpdatePassword(PasswordResetRequestDTO passwordResetRequestDTO, Usuario usuario) {
        if (!passwordResetRequestDTO.getNewPassword().equals(passwordResetRequestDTO.getConfirmNewPassword())) {
            throw new InvalidPasswordException("La nueva contraseña y su confirmación no coinciden");
        }

        // Validación de los requisitos de la nueva contraseña
        String regexPass = "^(?=\\w*\\d)(?=\\w*[a-z])\\S{8,16}$";
        Pattern patternPass = Pattern.compile(regexPass);
        Matcher matcherPass = patternPass.matcher(passwordResetRequestDTO.getNewPassword());

        if (!matcherPass.matches()) {
            throw new InvalidPasswordException("La nueva contraseña no cumple con los requisitos de seguridad: Debe tener al menos un número y un caracter");
        }

        usuario.setPassword(passwordEncoder.encode(passwordResetRequestDTO.getNewPassword()));
        usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);

        if (usuario.isPresent()) {
            return usuario.get();
        }

        throw new UsernameNotFoundException("User Not Found");
    }

    @Override
    public ResponseEntity<?> resetPassword(Long userId, PasswordResetRequestDTO passwordResetRequestDTO){
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(userId);
        if(usuarioOptional.isEmpty()){
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOptional.get();

        // Validación y actualización de la nueva contraseña
        validateAndUpdatePassword(passwordResetRequestDTO, usuario);

        return ResponseEntity.ok(new ApiResponse(true, "Contraseña restablecida exitosamente."));
    }
}
