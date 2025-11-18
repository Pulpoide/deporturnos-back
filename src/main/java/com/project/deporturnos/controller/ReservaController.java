package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.GeneralResponseDTO;
import com.project.deporturnos.entity.dto.ReservaRequestDTO;
import com.project.deporturnos.entity.dto.ReservaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.ReservaResponseDTO;
import com.project.deporturnos.service.IReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
public class ReservaController {

    private final IReservaService reservaService;

    // Endpoint para obtener todas las reservas
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<ReservaResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        Page<ReservaResponseDTO> dataPage = reservaService.getPaginatedData(page, size, sortBy);
        return ResponseEntity.ok(dataPage);
    }

    // Endpoint para obtener una única reserva por su ID
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reservaService.getById(id));
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
    public ResponseEntity<ReservaResponseDTO> update(@PathVariable("id") Long id,
            @Valid @RequestBody ReservaRequestUpdateDTO reservaRequestUpdateDTO) {
        return ResponseEntity.ok(reservaService.update(id, reservaRequestUpdateDTO));
    }

    // Endoint para eliminar reserva
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        reservaService.delete(id);
        return ResponseEntity.ok(new GeneralResponseDTO("Reserva eliminada correctamente."));
    }

    // Endpoint para mostrar reservas filtradas por fechaDesde || fechaHasta
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/filtrar")
    public ResponseEntity<Page<ReservaResponseDTO>> getReservasByFecha(
            @RequestParam(value = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(value = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha") String sortBy) {

        Page<ReservaResponseDTO> reservasFiltradas = reservaService.getReservasEntreFechas(
                fechaDesde,
                fechaHasta,
                page,
                size,
                sortBy);

        return ResponseEntity.ok(reservasFiltradas);
    }

    // Endpoint para "empezar" la reserva, cambiandola a estado "EN_PROCESO"
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/empezar")
    public ResponseEntity<String> empezarReserva(@PathVariable Long id) {
        reservaService.empezarReserva(id);
        return ResponseEntity.ok("Reserva iniciada exitosamente.");
    }

    // Endpoints para ROLE_CLIENTE o ROLE_ADMIN

    // Endpoint para registrar reserva
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @PostMapping("/byuser")
    public ResponseEntity<?> saveReservaByUser(@Valid @RequestBody ReservaRequestDTO reservaRequestDTO) {
        return ResponseEntity.ok(reservaService.saveReservaByUser(reservaRequestDTO));
    }

    // Endpoint para cancelar una reserva
    @PreAuthorize("hasRole('ROLE_CLIENTE') or hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancel(@PathVariable("id") Long id) {
        reservaService.cancel(id);
        return ResponseEntity.ok(new GeneralResponseDTO("Reserva cancelada."));
    }

}
