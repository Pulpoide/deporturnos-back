package com.project.deporturnos.controller;

import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.*;
import com.project.deporturnos.security.JwtService;
import com.project.deporturnos.security.PasswordResetTokenService;
import com.project.deporturnos.service.implementation.AuthService;
import com.project.deporturnos.service.implementation.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;


    // Endpoint para Registrar Usuario 2.0
    @PostMapping("/signup")
    public ResponseEntity<RegistrationResponseDTO> register(@RequestBody RegistrationRequestDTO registrationRequestDTO) {
        RegistrationResponseDTO registrationResponseDTO =  authService.signup(registrationRequestDTO);
        return ResponseEntity.ok(registrationResponseDTO);
    }

    // Endpoint para verificar email de usuario
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDTO verifyUserDto) {
        try {
            authService.verifyUser(verifyUserDto);
            return ResponseEntity.ok("Cuenta verificada exitosamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint para reenviar código de verificación
    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email){
        try {
            authService.resendVerificationCode(email);
            return ResponseEntity.ok("Código de verificación reenviado.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint para logear usuario
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginRequestDTO loginRequestDTO){
        Usuario authenticatedUser = authService.authenticate(loginRequestDTO);
        String jwtToken = jwtService.getToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getJwtExpirationTime(), loginRequestDTO.getEmail(), authenticatedUser.getId(), authenticatedUser.getNombre(), authenticatedUser.getTelefono(), authenticatedUser.isNotificaciones());
        return ResponseEntity.ok(loginResponse);
    }

    // Endpoint para solicitar restablecimiento de contraseña
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam("email") String email){
        Optional<Usuario> userOptional = usuarioService.findByEmail(email);
        if(userOptional.isPresent()){
            List<String> roles = List.of("CLIENTE");
            String token = passwordResetTokenService.generateToken(email, roles);
            passwordResetTokenService.sendResetToken(email, token);
            return ResponseEntity.ok("Fue enviado un link a tu email para restablecer tu contraseña.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
    }

}
