package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.LoginRequestDTO;
import com.project.deporturnos.entity.dto.RegistrationRequestDTO;
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


    // Endpoint para Registrar Usuario
    @PostMapping( "/register")
    public ResponseEntity<?> saveUsuario(@RequestBody RegistrationRequestDTO registrationRequestDTO) {
        return ResponseEntity.ok(authService.register(registrationRequestDTO));
    }

    // Endpoint para Logear Usuario
    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody LoginRequestDTO loginDTO) {
        return ResponseEntity.ok(authService.login(loginDTO));
    }
}
