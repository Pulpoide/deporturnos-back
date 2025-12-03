package com.project.deporturnos.controller;

import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.*;
import com.project.deporturnos.security.PasswordResetTokenService;
import com.project.deporturnos.service.IUsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones relacionadas con la administración y gestión de usuarios del sistema.")
public class UsuarioController {

    private final IUsuarioService usuarioService;
    private final PasswordResetTokenService passwordResetTokenService;

    // ====================================================
    // ADMIN ONLY
    // ====================================================

    @Operation(summary = "Listar usuarios", description = "Retorna una lista paginada de usuarios. Permite filtrar por nombre o email utilizando el parámetro opcional `search`.")
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    @ApiResponse(responseCode = "404", description = "No se encontraron usuarios", content = @Content(schema = @Schema(hidden = true)))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UsuarioSimpleDTO>> getAll(
            @Parameter(description = "Texto a buscar por nombre o email.", example = "joaquin") @RequestParam(name = "search", required = false) String search,

            @PageableDefault(page = 0, size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(usuarioService.getPaginatedData(pageable, search));
    }

    // ----------------------------------------------------

    @Operation(summary = "Actualizar usuario", description = "Modifica los datos de un usuario existente identificado por su ID.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> update(
            @Parameter(description = "ID del usuario a actualizar", example = "12") @PathVariable("id") Long id,

            @Valid @RequestBody UsuarioRequestUpdateDTO usuarioRequestUpdateDTO) {
        return ResponseEntity.ok(usuarioService.update(id, usuarioRequestUpdateDTO));
    }

    // ----------------------------------------------------

    @Operation(summary = "Eliminar usuario", description = "Elimina (soft delete) un usuario del sistema.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID del usuario a eliminar", example = "8") @PathVariable("id") Long id) {
        usuarioService.delete(id);
        return ResponseEntity.ok(new GeneralResponseDTO("Usuario eliminado correctamente"));
    }

    // ----------------------------------------------------

    @Operation(summary = "Cambiar rol de usuario", description = "Alterna entre el rol CLIENTE y ADMIN.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/role")
    public ResponseEntity<UsuarioResponseDTO> changeRole(
            @Parameter(description = "ID del usuario", example = "5") @PathVariable("id") Long id) {
        return ResponseEntity.ok(usuarioService.changeRole(id));
    }

    // ----------------------------------------------------

    @Operation(summary = "Bloquear o desbloquear usuario", description = "Activa o desactiva la cuenta del usuario.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/account")
    public ResponseEntity<LockUnlockResponseDTO> lockUnlock(
            @Parameter(description = "ID del usuario", example = "4") @PathVariable("id") Long id) {
        return ResponseEntity.ok(usuarioService.lockUnlock(id));
    }

    // ====================================================
    // ADMIN + CLIENTE
    // ====================================================

    @Operation(summary = "Actualizar perfil del usuario", description = "Permite que un usuario actualice sus datos personales.")
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/edit-profile")
    public ResponseEntity<ProfileResUpdateDTO> updateCurrentUser(
            @Parameter(description = "ID del usuario", example = "10") @PathVariable("id") Long id,

            @Valid @RequestBody ProfileReqUpdateDTO profileReqUpdateDTO) {
        return ResponseEntity.ok(usuarioService.updateProfile(id, profileReqUpdateDTO));
    }

    // ----------------------------------------------------

    @Operation(summary = "Listar reservas del usuario", description = "Obtiene las reservas del usuario según el estado seleccionado: FUTURAS, PASADAS o TODAS.")
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}/reservas")
    public ResponseEntity<Page<ReservaResponseDTO>> getUserReservations(
            @Parameter(description = "ID del usuario", example = "3") @PathVariable("id") Long id,

            @Parameter(description = "Estado de reservas a consultar", example = "FUTURAS") @RequestParam(defaultValue = "FUTURAS") String estado,

            Pageable pageable) {
        return ResponseEntity.ok(usuarioService.findReservations(id, estado, pageable));
    }

    // ----------------------------------------------------

    @Operation(summary = "Cambiar contraseña", description = "Permite a un usuario cambiar su propia contraseña.")
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequestDTO passwordChangeRequestDTO) {
        Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(usuarioService.changePassword(currentUser.getId(), passwordChangeRequestDTO));
    }

    // ----------------------------------------------------

    @Operation(summary = "Restablecer contraseña con token", description = "Permite establecer una nueva contraseña a partir de un token de recuperación válido.")
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Parameter(description = "Token de recuperación enviado al correo") @RequestParam("token") String token,

            @RequestBody PasswordResetRequestDTO passwordResetRequestDTO) {
        boolean isValid = passwordResetTokenService.validateToken(token);

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido o expirado.");
        }

        Usuario usuario = passwordResetTokenService.getUserByToken(token);
        return usuarioService.resetPassword(usuario.getId(), passwordResetRequestDTO);
    }
}
