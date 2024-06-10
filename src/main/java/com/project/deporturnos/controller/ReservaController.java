package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.ReservaDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ReservaController {

    // Endpoint para Registrar Reserva CLIENTE
    @PostMapping(path = "/clients/reservas/registrar")
    public ResponseEntity<?> saveReserva(@RequestBody ReservaDTO reservaDTO) {
        return null;
    }

    // Endpoint para Mostrar Reservas CLIENTE
    @GetMapping(path = "/clients/reservas")
    public ResponseEntity<?> getAllReservasByUser() {
        return null;
    }

    // Endpoint para Mostrar Reservas ADMINISTRADOR
    @GetMapping(path = "/admin/reservas")
    public ResponseEntity<?> getAllReservasAdmin() {
        return null;
    }



}
