package com.project.deporturnos.controller;

import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.*;
import com.project.deporturnos.security.JwtService;
import com.project.deporturnos.security.PasswordResetTokenService;
import com.project.deporturnos.service.implementation.AuthService;
import com.project.deporturnos.service.implementation.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints públicos para registro, inicio de sesión, verificación y recuperación de cuentas.")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final PasswordResetTokenService passwordResetTokenService;

    // ============================================================
    // AUTH FLOW
    // ============================================================

    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nuevo usuario en el sistema y envía un código de verificación al correo electrónico proporcionado.")
    @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente", content = @Content(schema = @Schema(implementation = RegistrationResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario ya existente", content = @Content(schema = @Schema(hidden = true)))
    @PostMapping("/signup")
    public ResponseEntity<RegistrationResponseDTO> register(
            @RequestBody RegistrationRequestDTO registrationRequestDTO) {

        RegistrationResponseDTO registrationResponseDTO = authService.signup(registrationRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationResponseDTO);
    }

    // ----------------------------------------------------

    @Operation(summary = "Verificar cuenta de usuario", description = "Valida el código de verificación enviado al correo para activar la cuenta.")
    @ApiResponse(responseCode = "200", description = "Cuenta verificada exitosamente")
    @ApiResponse(responseCode = "400", description = "Código inválido o expirado", content = @Content(schema = @Schema(hidden = true)))
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(
            @RequestBody VerifyUserDTO verifyUserDto) {

        try {
            authService.verifyUser(verifyUserDto);
            return ResponseEntity.ok("Cuenta verificada exitosamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ----------------------------------------------------

    @Operation(summary = "Reenviar código de verificación", description = "Envía un nuevo código de verificación al correo proporcionado si el anterior expiró.")
    @ApiResponse(responseCode = "200", description = "Código reenviado correctamente")
    @ApiResponse(responseCode = "400", description = "Error al reenviar el código", content = @Content(schema = @Schema(hidden = true)))
    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(
            @Parameter(description = "Email del usuario", example = "usuario@mail.com") @RequestParam String email) {
        try {
            authService.resendVerificationCode(email);
            return ResponseEntity.ok("Código de verificación reenviado.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ----------------------------------------------------

    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve un JWT válido junto con información básica del perfil.")
    @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content(schema = @Schema(hidden = true)))
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(
            @RequestBody LoginRequestDTO loginRequestDTO) {

        Usuario authenticatedUser = authService.authenticate(loginRequestDTO);

        String jwtToken = jwtService.getToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse(
                jwtToken,
                jwtService.getJwtExpirationTime(),
                loginRequestDTO.getEmail(),
                authenticatedUser.getId(),
                authenticatedUser.getNombre(),
                authenticatedUser.getTelefono(),
                authenticatedUser.isNotificaciones());

        return ResponseEntity.ok(loginResponse);
    }

    // ----------------------------------------------------

    @Operation(summary = "Solicitar restablecimiento de contraseña", description = "Envía un email con un link seguro para restablecer la contraseña del usuario.")
    @ApiResponse(responseCode = "200", description = "Email enviado correctamente")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(schema = @Schema(hidden = true)))
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Parameter(description = "Email del usuario", example = "usuario@mail.com") @RequestParam("email") String email) {

        Optional<Usuario> userOptional = usuarioService.findByEmail(email);

        if (userOptional.isPresent()) {
            List<String> roles = List.of("CLIENTE");
            String token = passwordResetTokenService.generateToken(email, roles);
            passwordResetTokenService.sendResetToken(email, token);
            return ResponseEntity.ok("Fue enviado un link a tu email para restablecer tu contraseña.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
    }
}