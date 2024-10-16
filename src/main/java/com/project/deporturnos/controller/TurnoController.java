package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.*;
import com.project.deporturnos.service.ITurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final ITurnoService turnoService;


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
        return ResponseEntity.ok(new GeneralResponseDTO("Turno eliminado correctamente."));
    }

    // Endpoint para cargar turnos masivamente
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/massive-charge")
    public ResponseEntity<String> cargaMasivaTurnos(@RequestBody CargaMasivaTurnosDTO cargaMasivaTurnosDTO){
        int turnosCreados = turnoService.cargaMasivaTurnos(cargaMasivaTurnosDTO);
        return ResponseEntity.ok(String.format("Carga masiva de turnos completada con éxito, se cargaron %d turnos.", turnosCreados));
    }

    // Endpoint para mostrar turnos filtrados por fechaDesde || fechaHasta
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/filtrar")
    public ResponseEntity<List<TurnoResponseDTO>> getTurnosByFecha(
            @RequestParam(value = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(value = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {

        List<TurnoResponseDTO> turnosFiltrados;

        if (fechaDesde == null && fechaHasta == null) {
            turnosFiltrados = turnoService.getAll();
        } else {
            turnosFiltrados = turnoService.getTurnosEntreFechas(fechaDesde, fechaHasta);
        }

        return ResponseEntity.ok(turnosFiltrados);
    }



    // Endpoints para ROLE_CLIENTE o ROLE_ADMIN

    // Endpoint para obtener todos los turnos con TurnoState.DISPONIBLE de una cancha en espécífico
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @GetMapping("/disponibles/{id}/cancha")
    public ResponseEntity<List<TurnoResponseDTO>> getAllAvailableByCanchaAndDate(@PathVariable("id") Long id, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(turnoService.getAllAvailableByCanchaAndDate(id, fecha));
    }

}
