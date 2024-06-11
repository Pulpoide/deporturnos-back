package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.TurnoRequestDTO;
import com.project.deporturnos.service.ITurnoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/turnos")
public class TurnoController {


    @Autowired
    ITurnoService turnoService;

    // Endpoint para Registrar Turno
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody TurnoRequestDTO turnoRequestDTO){
        return ResponseEntity.ok(turnoService.save(turnoRequestDTO));
    }


}
