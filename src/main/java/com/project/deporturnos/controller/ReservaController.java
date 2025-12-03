package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.GeneralResponseDTO;
import com.project.deporturnos.entity.dto.ReservaRequestDTO;
import com.project.deporturnos.entity.dto.ReservaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.ReservaResponseDTO;
import com.project.deporturnos.service.IReservaService;

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

@RestController
@CrossOrigin
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Operaciones relacionadas con la gestión, creación, cancelación y consulta de reservas.")
public class ReservaController {

    private final IReservaService reservaService;

    // ============================================================
    // ROLE_ADMIN — Gestión de Reservas
    // ============================================================

    @Operation(summary = "Listar reservas", description = "Obtiene una lista paginada de todas las reservas registradas en el sistema.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido con éxito")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<ReservaResponseDTO>> getAll(
            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Cantidad de elementos por página", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "id") @RequestParam(defaultValue = "id") String sortBy) {

        return ResponseEntity.ok(reservaService.getPaginatedData(page, size, sortBy));
    }

    // ----------------------------------------------------

    @Operation(summary = "Obtener reserva por ID", description = "Retorna los detalles de una reserva específica según su identificador único.")
    @ApiResponse(responseCode = "200", description = "Reserva encontrada")
    @ApiResponse(responseCode = "404", description = "Reserva no encontrada", content = @Content(schema = @Schema(hidden = true)))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> getById(
            @Parameter(description = "ID de la reserva", example = "1") @PathVariable("id") Long id) {

        return ResponseEntity.ok(reservaService.getById(id));
    }

    // ----------------------------------------------------

    @Operation(summary = "Registrar reserva (Admin)", description = "Crea una nueva reserva en el sistema desde el panel administrativo.")
    @ApiResponse(responseCode = "200", description = "Reserva creada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o conflicto de disponibilidad", content = @Content(schema = @Schema(hidden = true)))
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody ReservaRequestDTO reservaRequestDTO) {
        return ResponseEntity.ok(reservaService.save(reservaRequestDTO));
    }

    // ----------------------------------------------------

    @Operation(summary = "Actualizar reserva", description = "Modifica los datos de una reserva existente.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> update(
            @Parameter(description = "ID de la reserva a actualizar", example = "5") @PathVariable("id") Long id,
            @Valid @RequestBody ReservaRequestUpdateDTO reservaRequestUpdateDTO) {

        return ResponseEntity.ok(reservaService.update(id, reservaRequestUpdateDTO));
    }

    // ----------------------------------------------------

    @Operation(summary = "Eliminar reserva", description = "Elimina una reserva del sistema por su ID.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "ID de la reserva a eliminar", example = "5") @PathVariable("id") Long id) {

        reservaService.delete(id);
        return ResponseEntity.ok(new GeneralResponseDTO("Reserva eliminada correctamente."));
    }

    // ----------------------------------------------------

    @Operation(summary = "Filtrar reservas por fecha", description = "Obtiene reservas dentro de un rango de fechas, con paginación.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/filtrar")
    public ResponseEntity<Page<ReservaResponseDTO>> getReservasByFecha(
            @Parameter(description = "Fecha desde (formato YYYY-MM-DD)", example = "2023-10-01") @RequestParam(value = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,

            @Parameter(description = "Fecha hasta (formato YYYY-MM-DD)", example = "2023-10-31") @RequestParam(value = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,

            @Parameter(description = "Número de página", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Cantidad de elementos por página", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "fecha") @RequestParam(defaultValue = "fecha") String sortBy) {

        return ResponseEntity.ok(
                reservaService.getReservasEntreFechas(fechaDesde, fechaHasta, page, size, sortBy));
    }

    // ----------------------------------------------------

    @Operation(summary = "Iniciar proceso de reserva", description = "Cambia el estado de la reserva a EN_PROCESO.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/empezar")
    public ResponseEntity<String> empezarReserva(
            @Parameter(description = "ID de la reserva a iniciar", example = "10") @PathVariable Long id) {

        reservaService.empezarReserva(id);
        return ResponseEntity.ok("Reserva iniciada exitosamente.");
    }

    // ============================================================
    // CLIENTE + ADMIN — Acciones del usuario
    // ============================================================

    @Operation(summary = "Registrar reserva (Usuario)", description = "Permite a un cliente autenticado registrar su propia reserva.")
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @PostMapping("/byuser")
    public ResponseEntity<?> saveReservaByUser(
            @Valid @RequestBody ReservaRequestDTO reservaRequestDTO) {

        return ResponseEntity.ok(reservaService.saveReservaByUser(reservaRequestDTO));
    }

    // ----------------------------------------------------

    @Operation(summary = "Cancelar reserva", description = "El usuario o el administrador pueden cancelar una reserva existente.")
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancel(
            @Parameter(description = "ID de la reserva a cancelar", example = "12") @PathVariable("id") Long id) {

        reservaService.cancel(id);
        return ResponseEntity.ok(new GeneralResponseDTO("Reserva cancelada."));
    }
}