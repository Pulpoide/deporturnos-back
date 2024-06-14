package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.GeneralResponseDTO;
import com.project.deporturnos.entity.dto.LockUnlockResponseDTO;
import com.project.deporturnos.entity.dto.UsuarioRequestUpdateDTO;
import com.project.deporturnos.entity.dto.UsuarioResponseDTO;
import com.project.deporturnos.service.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;


    // Endpoint para obtener todos los usuarios
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(usuarioService.getAllUsuarios().getContent());
    }

    // Endpoint para actualizar usuario
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> update(@PathVariable Long id, @Valid @RequestBody UsuarioRequestUpdateDTO usuarioRequestUpdateDTO) {
        return ResponseEntity.ok(usuarioService.update(id, usuarioRequestUpdateDTO));
    }

    // Endoint para eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.ok( new GeneralResponseDTO("Usuario eliminado correctamente."));
    }

    // Endpoint para cambiar el rol de un usuario
    @PutMapping("/{id}/role")
    public ResponseEntity<UsuarioResponseDTO> changeRole(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.changeRole(id));
    }

    // Endpoint para bloquear/desbloquear usuario
    @PutMapping("/{id}/account")
    public ResponseEntity<LockUnlockResponseDTO> lockUnlock(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.lockUnlock(id));
    }
}
