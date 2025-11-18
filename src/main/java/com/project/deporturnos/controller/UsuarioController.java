package com.project.deporturnos.controller;

import com.project.deporturnos.entity.domain.Reserva;
import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.*;
import com.project.deporturnos.security.PasswordResetTokenService;
import com.project.deporturnos.service.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final IUsuarioService usuarioService;
    private final PasswordResetTokenService passwordResetTokenService;

    // Endpoints para ROLE_ADMIN ♫:

    // Endpoint para obtener todos los usuarios
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(usuarioService.getAll());
    }

    // Endpoint para actualizar usuario
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> update(@PathVariable("id") Long id,
            @Valid @RequestBody UsuarioRequestUpdateDTO usuarioRequestUpdateDTO) {
        return ResponseEntity.ok(usuarioService.update(id, usuarioRequestUpdateDTO));
    }

    // Endoint para eliminar usuario
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        usuarioService.delete(id);
        return ResponseEntity.ok(new GeneralResponseDTO("Usuario eliminado correctamente"));
    }

    // Endpoint para cambiar el rol de un usuario
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/role")
    public ResponseEntity<UsuarioResponseDTO> changeRole(@PathVariable("id") Long id) {
        return ResponseEntity.ok(usuarioService.changeRole(id));
    }

    // Endpoint para bloquear/desbloquear usuario
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/account")
    public ResponseEntity<LockUnlockResponseDTO> lockUnlock(@PathVariable("id") Long id) {
        return ResponseEntity.ok(usuarioService.lockUnlock(id));
    }

    // Endpoints para ROLE_CLIENTE

    // Actualizar Usuario
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/edit-profile")
    public ResponseEntity<ProfileResUpdateDTO> updateCurrentUser(@PathVariable("id") Long id,
            @Valid @RequestBody ProfileReqUpdateDTO profileReqUpdateDTO) {
        return ResponseEntity.ok(usuarioService.updateProfile(id, profileReqUpdateDTO));
    }

    // Listar Reservas de Usuario
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}/reservas")
    public ResponseEntity<Page<ReservaResponseDTO>> getUserReservations(@PathVariable("id") Long id,
            @RequestParam(value = "includeCompleted", required = false, defaultValue = "false") boolean includeCompleted,
            Pageable pageable) {
        return ResponseEntity.ok(usuarioService.findReservationsByUserIdPaginated(id, includeCompleted, pageable));
    }

    // Cambiar Contraseña
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequestDTO passwordChangeRequestDTO) {
        Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(usuarioService.changePassword(currentUser.getId(), passwordChangeRequestDTO));
    }

    // Restablecer Contraseña
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token,
            @RequestBody PasswordResetRequestDTO passwordResetRequestDTO) {
        boolean isValid = passwordResetTokenService.validateToken(token);

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido o expirado.");
        }
        Usuario usuario = passwordResetTokenService.getUserByToken(token);
        return usuarioService.resetPassword(usuario.getId(), passwordResetRequestDTO);
    }
}
