package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.LoginRequestDTO;
import com.project.deporturnos.entity.dto.RegistrationRequestDTO;
import com.project.deporturnos.service.implementation.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@CrossOrigin
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;


    // Endpoint para Registrar Usuario
//    @PostMapping("/register")
//    @Operation(summary = "Registrar un nuevo usuario")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = UsuarioDTO.class))),
//            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
//                    content = @Content),
//            @ApiResponse(responseCode = "409", description = "Usuario ya existe",
//                    content = @Content)
//    })
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
