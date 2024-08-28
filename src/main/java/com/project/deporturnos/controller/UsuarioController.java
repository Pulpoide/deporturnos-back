package com.project.deporturnos.controller;

import com.project.deporturnos.entity.domain.Reserva;
import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.*;
import com.project.deporturnos.security.PasswordResetTokenService;
import com.project.deporturnos.service.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    // Endpoint para obtener todos los usuarios
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(usuarioService.getAll());
    }

    // Endpoint para actualizar usuario
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> update(@PathVariable("id") Long id, @Valid @RequestBody UsuarioRequestUpdateDTO usuarioRequestUpdateDTO) {
        return ResponseEntity.ok(usuarioService.update(id, usuarioRequestUpdateDTO));
    }

    // Endoint para eliminar usuario
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        usuarioService.delete(id);
        return ResponseEntity.ok( new GeneralResponseDTO("Usuario eliminado correctamente"));
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



    // Endpoints para ROLE_CLIENTE o ROLE_ADMIN

    // Actualizar Usuario
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/edit-profile")
    public ResponseEntity<ProfileResUpdateDTO> updateCurrentUser(@PathVariable("id") Long id, @Valid @RequestBody ProfileReqUpdateDTO profileReqUpdateDTO) {
        return ResponseEntity.ok(usuarioService.updateProfile(id, profileReqUpdateDTO));
    }

    // Listar Reservas de Usuario
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}/reservas")
    public ResponseEntity<List<Reserva>> getUserReservations(@PathVariable("id") Long id) {
        return ResponseEntity.ok(usuarioService.findReservationsByUserId(id));
    }

    // Cambiar Contraseña
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequestDTO passwordChangeRequestDTO){
        Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(usuarioService.changePassword(currentUser.getId(), passwordChangeRequestDTO));
    }

    // Restablecer Contraseña
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token, @RequestBody PasswordResetRequestDTO passwordResetRequestDTO){
        boolean isValid  = passwordResetTokenService.validateToken(token);

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido o expirado.");
        }

        Usuario usuario = passwordResetTokenService.getUserByToken(token);
        return usuarioService.resetPassword(usuario.getId(), passwordResetRequestDTO);
    }


}
