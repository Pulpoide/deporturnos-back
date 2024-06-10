package com.project.deporturnos.controller;

import com.project.deporturnos.entity.dto.CanchaRequestDTO;
import com.project.deporturnos.entity.dto.CanchaResponseDTO;
import com.project.deporturnos.service.implementation.CanchaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/admin/canchas")
public class CanchaController {

    @Autowired
    CanchaService canchaService;

    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody CanchaRequestDTO canchaRequestDTO) {
        return ResponseEntity.ok(canchaService.save(canchaRequestDTO));
    }

    @GetMapping
    public ResponseEntity<List<CanchaResponseDTO>> findAll() {
        return ResponseEntity.ok(canchaService.getAll());
    }

}
