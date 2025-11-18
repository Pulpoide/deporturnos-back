package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.*;
import com.project.deporturnos.entity.dto.*;
import com.project.deporturnos.exception.*;
import com.project.deporturnos.repository.IReservaRepository;
import com.project.deporturnos.repository.IUsuarioRepository;
import com.project.deporturnos.service.IUsuarioService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequiredArgsConstructor
public class UsuarioService implements IUsuarioService, UserDetailsService {

    private final IUsuarioRepository usuarioRepository;
    private final IReservaRepository reservaRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper mapper;
    private static final Long SUPER_ADMIN_ID = 1L;

    @Override
    public Page<UsuarioSimpleDTO> getPaginatedData(Pageable pageable) {
        Page<Usuario> usuariosPage = usuarioRepository.findAllByDeletedFalse(pageable);

        if (usuariosPage.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron usuarios para listar.");
        }

        return usuariosPage.map(usuario -> mapper.convertValue(usuario, UsuarioSimpleDTO.class));
    }

    @Override
    public void delete(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        if (id.equals(SUPER_ADMIN_ID)) {
            throw new BusinessRuleException("No es posible eliminar al administrador supremo.");
        }

        usuario.setDeleted(true);
        usuario.setActivada(false);

        usuario.getReservas().forEach(reserva -> {
            reserva.setDeleted(true);
            Turno turno = reserva.getTurno();
            boolean allDeleted = turno.getReservas().stream().allMatch(Reserva::isDeleted);
            if (allDeleted) {
                turno.setEstado(TurnoState.DISPONIBLE);
            }
        });

        usuarioRepository.save(usuario);
    }

    @Override
    public UsuarioResponseDTO update(Long id, UsuarioRequestUpdateDTO usuarioRequestUpdateDTO) {
        return getUsuarioResponseDTO(id, usuarioRequestUpdateDTO);
    }

    private UsuarioResponseDTO getUsuarioResponseDTO(Long id, UsuarioRequestUpdateDTO usuarioRequestUpdateDTO) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if (usuarioOptional.isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOptional.get();

        if (usuarioRequestUpdateDTO.getNombre() != null) {
            usuario.setNombre(usuarioRequestUpdateDTO.getNombre());
        }

        if (usuarioRequestUpdateDTO.getEmail() != null) {

            // Validación de email
            String regex = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,}$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(usuarioRequestUpdateDTO.getEmail());
            if (!matcher.matches()) {
                throw new InvalidEmailException("Correo electrónico no válido.");
            }

            Optional<Usuario> usuarioOptional2 = usuarioRepository.findByEmail(usuarioRequestUpdateDTO.getEmail());
            if (usuarioOptional2.isPresent()) {
                throw new UserAlreadyExistsException("Ya existe un usuario con ese email.");
            }

            usuario.setEmail(usuarioRequestUpdateDTO.getEmail());
        }

        if (usuarioRequestUpdateDTO.getPassword() != null) {

            // Validación de password
            String regexPass = "^(?=\\w*\\d)(?=\\w*[a-z])\\S{8,16}$";
            Pattern patternPass = Pattern.compile(regexPass);
            Matcher matcherPass = patternPass.matcher(usuarioRequestUpdateDTO.getPassword());
            if (!matcherPass.matches()) {
                throw new InvalidPasswordException("Contraseña no válida.");
            }

            usuario.setPassword(passwordEncoder.encode(usuarioRequestUpdateDTO.getPassword()));
        }

        if (usuarioRequestUpdateDTO.getTelefono() != null) {
            usuario.setTelefono(usuarioRequestUpdateDTO.getTelefono());
        }

        if (usuarioRequestUpdateDTO.getNotificaciones() != null) {
            usuario.setNotificaciones(usuarioRequestUpdateDTO.getNotificaciones());
        }

        Usuario usuarioSaved = usuarioRepository.save(usuario);
        return mapper.convertValue(usuarioSaved, UsuarioResponseDTO.class);
    }

    @Override
    public UsuarioResponseDTO changeRole(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        if (id.equals(SUPER_ADMIN_ID)) {
            throw new BusinessRuleException("No es posible cambiar el rol al administrador supremo.");
        }

        if (usuario.getRol().equals(Rol.ADMIN)) {
            usuario.setRol(Rol.CLIENTE);
        } else {
            usuario.setRol(Rol.ADMIN);
        }

        Usuario saved = usuarioRepository.save(usuario);
        return mapper.convertValue(saved, UsuarioResponseDTO.class);
    }

    @Override
    public LockUnlockResponseDTO lockUnlock(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        if (id.equals(SUPER_ADMIN_ID)) {
            throw new BusinessRuleException("No es posible bloquear al administrador supremo.");
        }

        usuario.setActivada(!usuario.isActivada());

        Usuario saved = usuarioRepository.save(usuario);
        return mapper.convertValue(saved, LockUnlockResponseDTO.class);
    }

    @Override
    public Page<ReservaResponseDTO> findReservationsByUserIdPaginated(Long id, boolean includeCompleted,
            Pageable pageable) {
        // Get current user from SecurityContext
        Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser.getRol().equals(Rol.CLIENTE)) {
            if (!Objects.equals(id, currentUser.getId())) {
                throw new InsufficientAuthenticationException("No autorizado.");
            }
        }

        Page<Reserva> reservasPage;
        if (includeCompleted) {
            reservasPage = reservaRepository.findByUsuarioIdAndDeletedFalse(id, pageable);
        } else {
            reservasPage = reservaRepository.findByUsuarioIdAndEstadoNotAndDeletedFalse(id, ReservaState.COMPLETADA,
                    pageable);
        }
        return reservasPage.map(this::mapToReservaResponseDTO);
    }

    private ReservaResponseDTO mapToReservaResponseDTO(Reserva reserva) {
    if (reserva == null) {
        return null;
    }
    return mapper.convertValue(reserva, ReservaResponseDTO.class);
}

    @Override
    public ProfileResUpdateDTO updateProfile(Long id, ProfileReqUpdateDTO profileReqUpdateDTO) {

        Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser.getRol().equals(Rol.CLIENTE)) {
            if (!id.equals(currentUser.getId())) {
                throw new InsufficientAuthenticationException("No autorizado.");
            }
        }

        if (profileReqUpdateDTO.getNombre() != null) {
            currentUser.setNombre(profileReqUpdateDTO.getNombre());
        }
        if (profileReqUpdateDTO.getTelefono() != null) {
            currentUser.setTelefono(profileReqUpdateDTO.getTelefono());
        }

        currentUser.setNotificaciones(profileReqUpdateDTO.isNotificaciones());

        Usuario usuarioSaved = usuarioRepository.save(currentUser);

        return mapper.convertValue(usuarioSaved, ProfileResUpdateDTO.class);
    }

    @Override
    public ResponseEntity<?> changePassword(Long id, PasswordChangeRequestDTO passwordChangeRequestDTO) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);

        if (usuarioOptional.isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOptional.get();

        // Verificación de la contraseña actual
        if (!passwordEncoder.matches(passwordChangeRequestDTO.getOldPassword(), usuario.getPassword())) {
            throw new InvalidPasswordException("Contraseña actual incorrecta.");
        }

        // Validación y actualización de la nueva contraseña
        validateAndUpdatePassword(mapper.convertValue(passwordChangeRequestDTO, PasswordResetRequestDTO.class),
                usuario);

        return ResponseEntity.ok(new ApiResponse(true, "Contraseña cambiada exitosamente."));
    }

    private void validateAndUpdatePassword(PasswordResetRequestDTO passwordResetRequestDTO, Usuario usuario) {
        if (!passwordResetRequestDTO.getNewPassword().equals(passwordResetRequestDTO.getConfirmNewPassword())) {
            throw new InvalidPasswordException("La nueva contraseña y su confirmación no coinciden.");
        }

        // Validación de los requisitos de la nueva contraseña
        String regexPass = "^(?=\\w*\\d)(?=\\w*[a-z])\\S{8,16}$";
        Pattern patternPass = Pattern.compile(regexPass);
        Matcher matcherPass = patternPass.matcher(passwordResetRequestDTO.getNewPassword());

        if (!matcherPass.matches()) {
            throw new InvalidPasswordException(
                    "La nueva contraseña no cumple con los requisitos de seguridad: Minimo ocho caracteres, al menos un número y una letra.");
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

        throw new UsernameNotFoundException("User Not Found.");
    }

    @Override
    public ResponseEntity<?> resetPassword(Long userId, PasswordResetRequestDTO passwordResetRequestDTO) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(userId);
        if (usuarioOptional.isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOptional.get();

        // Validación y actualización de la nueva contraseña
        validateAndUpdatePassword(passwordResetRequestDTO, usuario);

        return ResponseEntity.ok(new ApiResponse(true, "Contraseña restablecida exitosamente."));
    }
}
