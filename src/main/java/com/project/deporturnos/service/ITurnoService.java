package com.project.deporturnos.service;

import com.project.deporturnos.entity.dto.CargaMasivaTurnosDTO;
import com.project.deporturnos.entity.dto.TurnoRequestDTO;
import com.project.deporturnos.entity.dto.TurnoRequestUpdateDTO;
import com.project.deporturnos.entity.dto.TurnoResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ITurnoService {
    TurnoResponseDTO save(TurnoRequestDTO turnoRequestDTO);

    List<TurnoResponseDTO> getAll();

    TurnoResponseDTO update(Long id, TurnoRequestUpdateDTO turnoRequestUpdateDTO);

    void delete(Long id);

    List<TurnoResponseDTO> getAllAvailableByCanchaAndDate(Long id, LocalDate fecha);

    void cargaMasivaTurnos(CargaMasivaTurnosDTO cargaMasivaTurnosDTO);

    List<TurnoResponseDTO> getTurnosEntreFechas(LocalDate fechaDesde, LocalDate fechaHasta);
}
