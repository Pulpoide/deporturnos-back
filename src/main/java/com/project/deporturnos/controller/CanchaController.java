package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.CanchaRequestDTO;
import com.project.deporturnos.entity.dto.CanchaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.CanchaResponseDTO;
import com.project.deporturnos.entity.dto.GeneralResponseDTO;
import com.project.deporturnos.service.ICanchaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Canchas", description = "Operaciones relacionadas con la administración, creación y consulta de canchas deportivas.")
public class CanchaController {

    private final ICanchaService canchaService;

    // ============================================================
    // ADMIN ENDPOINTS
    // ============================================================

    @Operation(summary = "Obtener todas las canchas", description = "Devuelve una lista completa de todas las canchas registradas en el sistema.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<CanchaResponseDTO>> getAll() {
        return ResponseEntity.ok(canchaService.getAll());
    }

    // ----------------------------------------------------

    @Operation(summary = "Registrar una nueva cancha", description = "Crea una cancha nueva con los datos enviados en el cuerpo de la solicitud.")
    @ApiResponse(responseCode = "200", description = "Cancha creada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(hidden = true)))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> save(
            @Valid @RequestBody CanchaRequestDTO canchaRequestDTO) {
        return ResponseEntity.ok(canchaService.save(canchaRequestDTO));
    }

    // ----------------------------------------------------

    @Operation(summary = "Actualizar una cancha", description = "Modifica los datos de una cancha existente.")
    @ApiResponse(responseCode = "200", description = "Cancha actualizada correctamente")
    @ApiResponse(responseCode = "404", description = "Cancha no encontrada", content = @Content(schema = @Schema(hidden = true)))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CanchaResponseDTO> update(
            @Parameter(description = "ID de la cancha a modificar", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody CanchaRequestUpdateDTO canchaRequestUpdateDTO) {

        return ResponseEntity.ok(canchaService.update(id, canchaRequestUpdateDTO));
    }

    // ----------------------------------------------------

    @Operation(summary = "Eliminar una cancha", description = "Elimina una cancha por su ID.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID de la cancha a eliminar", example = "1") @PathVariable("id") Long id) {

        canchaService.delete(id);
        return ResponseEntity.ok(new GeneralResponseDTO("Cancha eliminada correctamente."));
    }

    // ============================================================
    // CLIENTE + ADMIN ENDPOINTS
    // ============================================================

    @Operation(summary = "Obtener canchas disponibles por deporte", description = "Devuelve el listado de canchas que se encuentran disponibles para el deporte indicado.")
    @ApiResponse(responseCode = "200", description = "Canchas disponibles obtenidas correctamente")
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @GetMapping("/disponibles/{deporte}")
    public ResponseEntity<List<CanchaResponseDTO>> getAvailableByDeporte(
            @Parameter(description = "Nombre del deporte (FUTBOL, PADEL, TENIS)", example = "FUTBOL") @PathVariable String deporte) {

        return ResponseEntity.ok(canchaService.getAvailableByDeporte(deporte));
    }
}