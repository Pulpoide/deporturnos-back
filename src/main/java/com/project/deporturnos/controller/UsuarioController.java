package com.project.deporturnos.controller;

import com.project.deporturnos.entity.domain.Reserva;
import com.project.deporturnos.entity.dto.GeneralResponseDTO;
import com.project.deporturnos.entity.dto.LockUnlockResponseDTO;
import com.project.deporturnos.entity.dto.UsuarioRequestUpdateDTO;
import com.project.deporturnos.entity.dto.UsuarioResponseDTO;
import com.project.deporturnos.service.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;

    // Endpoint para obtener todos los usuarios
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(usuarioService.getAllUsuarios().getContent());
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
    @PutMapping("/{id}/profile")
    public ResponseEntity<UsuarioResponseDTO> updateCurrentUser(@PathVariable("id") Long id, @Valid @RequestBody UsuarioRequestUpdateDTO usuarioRequestUpdateDTO) {
        return ResponseEntity.ok(usuarioService.update(id, usuarioRequestUpdateDTO));
    }

    // Listar Reservas de Usuario
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}/reservas")
    public ResponseEntity<List<Reserva>> getUserReservations(@PathVariable("id") Long id) {
        return ResponseEntity.ok(usuarioService.findReservationsByUserId(id));
    }
}
