package com.project.deporturnos.service;

import com.project.deporturnos.entity.dto.TurnoRequestDTO;
import com.project.deporturnos.entity.dto.TurnoRequestUpdateDTO;
import com.project.deporturnos.entity.dto.TurnoResponseDTO;

import java.util.List;

public interface ITurnoService {
    TurnoResponseDTO save(TurnoRequestDTO turnoRequestDTO);

    List<TurnoResponseDTO> getAll();

    TurnoResponseDTO update(Long id, TurnoRequestUpdateDTO turnoRequestUpdateDTO);

    void delete(Long id);

    List<TurnoResponseDTO> getAllAvailable(Long id);
}
