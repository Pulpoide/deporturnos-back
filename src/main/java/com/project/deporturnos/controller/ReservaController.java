package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.GeneralResponseDTO;
import com.project.deporturnos.entity.dto.ReservaRequestDTO;
import com.project.deporturnos.entity.dto.ReservaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.ReservaResponseDTO;
import com.project.deporturnos.service.IReservaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    IReservaService reservaService;

    // Endpoint para obtener todas las reservas
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> getAll() {
        return ResponseEntity.ok(reservaService.getAll());
    }

    // Endpoint para Registrar Reserva
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody ReservaRequestDTO reservaRequestDTO) {
        return ResponseEntity.ok(reservaService.save(reservaRequestDTO));
    }

    // Endpoint para actualizar una reserva
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ReservaRequestUpdateDTO reservaRequestUpdateDTO) {
        return ResponseEntity.ok(reservaService.update(id, reservaRequestUpdateDTO));
    }

    // Endoint para eliminar reserva
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        reservaService.delete(id);
        return ResponseEntity.ok(new GeneralResponseDTO("Reserva eliminada correctamente."));
    }



    // Endpoints para ROLE_CLIENTE o ROLE_ADMIN

    // Endpoint para registrar reserva
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @PostMapping("/byuser")
    public ResponseEntity<?> saveReservaByUser(@Valid @RequestBody ReservaRequestDTO reservaRequestDTO) {
        return ResponseEntity.ok(reservaService.saveReservaByUser(reservaRequestDTO));
    }


}
