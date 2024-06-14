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



    // Endpoint para obtener todas las canchas
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // nofunca
    public ResponseEntity<List<TurnoResponseDTO>> getAll() {
        return ResponseEntity.ok(turnoService.getAll());
    }

    // Endpoint para Registrar Turno
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody TurnoRequestDTO turnoRequestDTO){
        return ResponseEntity.ok(turnoService.save(turnoRequestDTO));
    }

    // Endpoint para actualizar una turno
    @PutMapping("/{id}")
    public ResponseEntity<TurnoResponseDTO> update(@PathVariable Long id, @Valid @RequestBody TurnoRequestUpdateDTO turnoRequestUpdateDTO) {
        return ResponseEntity.ok(turnoService.update(id, turnoRequestUpdateDTO));
    }

    // Endoint para eliminar turno
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        turnoService.delete(id);
        return ResponseEntity.ok(new GeneralResponseDTO("Turno eliminado correctamente."));
    }


}
