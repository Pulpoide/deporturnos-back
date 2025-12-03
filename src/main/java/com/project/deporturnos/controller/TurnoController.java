package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.*;
import com.project.deporturnos.service.ITurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
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
@Tag(name = "Turnos", description = "Operaciones relacionadas con la gestión, asignación y consulta de disponibilidad de turnos.")
public class TurnoController {

    private final ITurnoService turnoService;

    // ============================================================
    // ROLE_ADMIN — Gestión de turnos
    // ============================================================

    @Operation(summary = "Listar turnos", description = "Obtiene una lista paginada de todos los turnos registrados en el sistema.")
    @ApiResponse(responseCode = "200", description = "Lista obtenida con éxito")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<TurnoResponseDTO>> getAll(
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Cantidad de elementos por página", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "id") @RequestParam(defaultValue = "id") String sortBy) {

        return ResponseEntity.ok(turnoService.getPaginatedData(page, size, sortBy));
    }

    // ----------------------------------------------------

    @Operation(summary = "Registrar turno", description = "Crea un nuevo turno individual en el sistema.")
    @ApiResponse(responseCode = "200", description = "Turno creado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o conflicto de horario", content = @Content(schema = @Schema(hidden = true)))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody TurnoRequestDTO turnoRequestDTO) {
        return ResponseEntity.ok(turnoService.save(turnoRequestDTO));
    }

    // ----------------------------------------------------

    @Operation(summary = "Actualizar turno", description = "Modifica los datos (horario, precio, estado) de un turno existente.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TurnoResponseDTO> update(
            @Parameter(description = "ID del turno a actualizar", example = "15") @PathVariable("id") Long id,
            @Valid @RequestBody TurnoRequestUpdateDTO turnoRequestUpdateDTO) {

        return ResponseEntity.ok(turnoService.update(id, turnoRequestUpdateDTO));
    }

    // ----------------------------------------------------

    @Operation(summary = "Eliminar turno", description = "Elimina un turno del sistema por su ID.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID del turno a eliminar", example = "15") @PathVariable("id") Long id) {

        turnoService.delete(id);
        return ResponseEntity.ok(new GeneralResponseDTO("Turno eliminado correctamente."));
    }

    // ----------------------------------------------------

    @Operation(summary = "Carga masiva de turnos", description = "Genera múltiples turnos automáticamente según un rango de fechas y horarios.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/massive-charge")
    public ResponseEntity<String> cargaMasivaTurnos(
            @RequestBody CargaMasivaTurnosDTO cargaMasivaTurnosDTO) {

        int turnosCreados = turnoService.cargaMasivaTurnos(cargaMasivaTurnosDTO);
        return ResponseEntity.ok(
                String.format("Carga masiva de turnos completada con éxito, se cargaron %d turnos.", turnosCreados));
    }

    // ----------------------------------------------------

    @Operation(summary = "Filtrar turnos por fecha", description = "Obtiene turnos dentro de un rango de fechas específico, con paginación.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/filtrar")
    public ResponseEntity<Page<TurnoResponseDTO>> getTurnosByFecha(
            @Parameter(description = "Fecha desde (formato YYYY-MM-DD)", example = "2023-11-01") @RequestParam(value = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,

            @Parameter(description = "Fecha hasta (formato YYYY-MM-DD)", example = "2023-11-30") @RequestParam(value = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,

            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Cantidad de elementos", example = "10") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "fecha") @RequestParam(defaultValue = "fecha") String sortBy) {

        return ResponseEntity.ok(
                turnoService.getTurnosEntreFechas(fechaDesde, fechaHasta, page, size, sortBy));
    }

    // ============================================================
    // CLIENTE + ADMIN — Consultas de disponibilidad
    // ============================================================

    @Operation(summary = "Turnos disponibles de una cancha", description = "Devuelve la lista de turnos con estado DISPONIBLE para una cancha y una fecha específica.")
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @GetMapping("/disponibles/{id}/cancha")
    public ResponseEntity<List<TurnoResponseDTO>> getAllAvailableByCanchaAndDate(
            @Parameter(description = "ID de la cancha", example = "3") @PathVariable("id") Long id,
            @Parameter(description = "Fecha a consultar (formato YYYY-MM-DD)", example = "2023-11-15") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(turnoService.getAllAvailableByCanchaAndDate(id, fecha));
    }
}