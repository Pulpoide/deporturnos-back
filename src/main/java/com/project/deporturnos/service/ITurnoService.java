package com.project.deporturnos.service;

import com.project.deporturnos.entity.dto.CargaMasivaTurnosDTO;
import com.project.deporturnos.entity.dto.TurnoRequestDTO;
import com.project.deporturnos.entity.dto.TurnoRequestUpdateDTO;
import com.project.deporturnos.entity.dto.TurnoResponseDTO;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

public interface ITurnoService {
    TurnoResponseDTO save(TurnoRequestDTO turnoRequestDTO);

    Page<TurnoResponseDTO> getPaginatedData(int page, int size, String sortBy);

    TurnoResponseDTO update(Long id, TurnoRequestUpdateDTO turnoRequestUpdateDTO);

    void delete(Long id);

    List<TurnoResponseDTO> getAllAvailableByCanchaAndDate(Long id, LocalDate fecha);

    int cargaMasivaTurnos(CargaMasivaTurnosDTO cargaMasivaTurnosDTO);

    Page<TurnoResponseDTO> getTurnosEntreFechas(LocalDate fechaDesde, LocalDate fechaHasta, int page, int size, String sortBy);
}
