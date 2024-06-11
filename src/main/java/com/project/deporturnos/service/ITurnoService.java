package com.project.deporturnos.service;

import com.project.deporturnos.entity.dto.TurnoRequestDTO;
import com.project.deporturnos.entity.dto.TurnoResponseDTO;

public interface ITurnoService {
    TurnoResponseDTO save(TurnoRequestDTO turnoRequestDTO);
}
