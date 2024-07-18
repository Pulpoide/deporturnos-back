package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.GeneralResponseDTO;
import com.project.deporturnos.entity.dto.TurnoRequestDTO;
import com.project.deporturnos.entity.dto.TurnoRequestUpdateDTO;
import com.project.deporturnos.entity.dto.TurnoResponseDTO;
import com.project.deporturnos.service.ITurnoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/turnos")
public class TurnoController {

    @Autowired
    ITurnoService turnoService;


    // Endpoint para obtener todos los turnos
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<TurnoResponseDTO>> getAll() {
        return ResponseEntity.ok(turnoService.getAll());
    }

    // Endpoint para Registrar Turno
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody TurnoRequestDTO turnoRequestDTO){
        return ResponseEntity.ok(turnoService.save(turnoRequestDTO));
    }

    // Endpoint para actualizar un turno
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TurnoResponseDTO> update(@PathVariable("id") Long id, @Valid @RequestBody TurnoRequestUpdateDTO turnoRequestUpdateDTO) {
        return ResponseEntity.ok(turnoService.update(id, turnoRequestUpdateDTO));
    }

    // Endoint para eliminar turno
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        turnoService.delete(id);
        return ResponseEntity.ok(new GeneralResponseDTO("Turno eliminado correctamente"));
    }


    // Endpoints para ROLE_CLIENTE o ROLE_ADMIN

    // Endpoint para obtener todos los turnos con TurnoState.DISPONIBLE de una cancha en espécífico
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @GetMapping("/disponibles/{id}/cancha")
    public ResponseEntity<List<TurnoResponseDTO>> getAllAvailable(@PathVariable("id") Long id) {
        return ResponseEntity.ok(turnoService.getAllAvailable(id));
    }



}
