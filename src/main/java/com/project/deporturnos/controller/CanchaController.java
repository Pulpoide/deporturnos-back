package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.CanchaRequestDTO;
import com.project.deporturnos.entity.dto.CanchaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.CanchaResponseDTO;
import com.project.deporturnos.entity.dto.GeneralResponseDTO;
import com.project.deporturnos.service.implementation.CanchaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/canchas")
public class CanchaController {

    @Autowired
    CanchaService canchaService;


    // Endpoint para obtener todas las canchas
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // nofunca
    public ResponseEntity<List<CanchaResponseDTO>> getAll() {
        return ResponseEntity.ok(canchaService.getAll());
    }

    // Endpoint para Registrar Cancha
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody CanchaRequestDTO canchaRequestDTO) {
        return ResponseEntity.ok(canchaService.save(canchaRequestDTO));
    }

    // Endpoint para actualizar una cancha
    @PutMapping("/{id}")
    public ResponseEntity<CanchaResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CanchaRequestUpdateDTO canchaRequestUpdateDTO) {
        return ResponseEntity.ok(canchaService.update(id, canchaRequestUpdateDTO));
    }

    // Endoint para eliminar cancha
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        canchaService.delete(id);
        return ResponseEntity.ok(new GeneralResponseDTO("Cancha eliminada correctamente."));
    }
}
