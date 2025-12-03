package com.project.deporturnos.controller;

import com.project.deporturnos.service.IReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@CrossOrigin
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Operaciones relacionadas con la generación de reportes y estadísticas del sistema.")
public class ReportsController {

    private final IReportsService reportsService;


    @Operation(summary = "Calcula las ganancias del centro deportivo en un rango de fechas")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/calculate-profits")
    public ResponseEntity<BigDecimal> calcularProfits(
            @RequestParam(value = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(value = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta
    ) {
        BigDecimal ganancias = reportsService.calcularGananciasGenerales(fechaDesde, fechaHasta);
        return ResponseEntity.ok(ganancias);
    }


}
