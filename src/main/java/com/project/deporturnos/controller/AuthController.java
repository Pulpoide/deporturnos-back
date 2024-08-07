package com.project.deporturnos.controller;

import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.*;
import com.project.deporturnos.security.JwtService;
import com.project.deporturnos.service.implementation.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;


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
            return ResponseEntity.ok("Cuenta verificada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // Endpoint para reenviar código de verificación
    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email){
        try {
            authService.resendVerificationCode(email);
            return ResponseEntity.ok("Código de verificación reenviado");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // Endpoint para logear usuario
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginRequestDTO loginRequestDTO){
        Usuario authenticatedUser = authService.authenticate(loginRequestDTO);
        String jwtToken = jwtService.getToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getJwtExpirationTime(), loginRequestDTO.getEmail(), authenticatedUser.getId(), authenticatedUser.getNombre());
        return ResponseEntity.ok(loginResponse);
    }

//    // Endpoint para Registrar Usuario
//    @PostMapping( "/register")
//    public ResponseEntity<?> saveUsuario(@RequestBody RegistrationRequestDTO registrationRequestDTO) {
//        return ResponseEntity.ok(authService.register(registrationRequestDTO));
//    }
//
//    // Endpoint para Logear Usuario
//    @PostMapping("/login")
//    public ResponseEntity<?> loginUsuario(@RequestBody LoginRequestDTO loginDTO) {
//        return ResponseEntity.ok(authService.login(loginDTO));
//    }
}
