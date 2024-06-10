package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.UsuarioRequestUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.project.deporturnos.entity.dto.LoginRequestDTO;
import com.project.deporturnos.entity.dto.UsuarioDTO;
import com.project.deporturnos.service.IUsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;


@RestController
@CrossOrigin
@RequestMapping("/api")
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;


    // Endpoint para Registrar Usuario
    @PostMapping(path = "/clients/registrar")
    public ResponseEntity<?> saveUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        return ResponseEntity.ok(usuarioService.addUsuario(usuarioDTO));
    }

    // Endpoint para Logear Usuario
    @PostMapping(path="/clients/login")
    public ResponseEntity<?> loginUsuario(@RequestBody LoginRequestDTO loginDTO) {
            return ResponseEntity.ok(usuarioService.loginUsuario(loginDTO));
    }

    // Endpoint para actualizar usuario
    @PutMapping("/clients/{id}")
    public ResponseEntity<UsuarioDTO> updateUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioRequestUpdateDTO usuarioRequestUpdateDTO) {
        return ResponseEntity.ok(usuarioService.update(id, usuarioRequestUpdateDTO));
    }

    // Endpoint para obtener todos los usuarios
    @GetMapping(path = "/admin/usuarios")
    public ResponseEntity<?> getAllUsuarios() {
        return ResponseEntity.ok(usuarioService.getAllUsuarios().getContent());
    }

    // Endoint para eliminar un usuario
    @DeleteMapping("/admin/usuarios/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }




























    // Viejo login
//    @GetMapping(path="/all")
//    public ResponseEntity<Page<Usuario>> getAllUsuarios(
//        // @RequestParam(required = false, defaultValue = "0") Integer page,
//        // @RequestParam(required = false, defaultValue = "10") Integer size,
//        // @RequestParam(required = false, defaultValue = "false") Boolean enablePagination
//        ){
//        return ResponseEntity.ok(usuarioService.getAllUsuarios());
//    }
//
//
//    @PostMapping(path = "/save")
//    public String saveUsuario(@RequestBody UsuarioDTO usuarioDTO) {
//
//        String id = usuarioService.addUsuario(usuarioDTO);
//        return id;
//    }
//
//    @PostMapping(path="/login")
//    public ResponseEntity<?> loginUsuario(@RequestBody LoginResponseDTO loginDTO) {
//        LoginMessageDTO loginMessage = usuarioService.loginUsuario(loginDTO);
//        return ResponseEntity.ok(loginMessage);
//    }

    
}
