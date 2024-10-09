package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.CanchaRequestDTO;
import com.project.deporturnos.entity.dto.CanchaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.CanchaResponseDTO;
import com.project.deporturnos.entity.dto.GeneralResponseDTO;
import com.project.deporturnos.service.ICanchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/canchas")
@RequiredArgsConstructor
public class CanchaController {

    private final ICanchaService canchaService;


    // Endpoint para obtener todas las canchas
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<CanchaResponseDTO>> getAll() {
        return ResponseEntity.ok(canchaService.getAll());
    }

    // Endpoint para Registrar Cancha
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody CanchaRequestDTO canchaRequestDTO) {
        return ResponseEntity.ok(canchaService.save(canchaRequestDTO));
    }

    // Endpoint para actualizar una cancha
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CanchaResponseDTO> update(@PathVariable("id") Long id, @Valid @RequestBody CanchaRequestUpdateDTO canchaRequestUpdateDTO) {
        return ResponseEntity.ok(canchaService.update(id, canchaRequestUpdateDTO));
    }

    // Endoint para eliminar cancha
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        canchaService.delete(id);
        return ResponseEntity.ok(new GeneralResponseDTO("Cancha eliminada correctamente."));
    }

    // Endpoints para ROLE_CLIENTE o ROLE_ADMIN

    // Endpoint para obtener todas las canchas disponibles
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @GetMapping("/disponibles/{deporte}")
    public ResponseEntity<List<CanchaResponseDTO>> getAvailableByDeporte(@PathVariable String deporte) {
        return ResponseEntity.ok(canchaService.getAvailableByDeporte(deporte));
    }
}
