package com.project.deporturnos.service;

import com.project.deporturnos.entity.dto.ReservaRequestDTO;
import com.project.deporturnos.entity.dto.ReservaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.ReservaResponseDTO;

import java.time.LocalDate;

import org.springframework.data.domain.Page;

public interface IReservaService {
    ReservaResponseDTO save(ReservaRequestDTO reservaRequestDTO);

    Page<ReservaResponseDTO> getPaginatedData(int page, int size, String sortBy);

    ReservaResponseDTO update(Long id, ReservaRequestUpdateDTO reservaRequestUpdateDTO);

    void delete(Long id);

    ReservaResponseDTO saveReservaByUser(ReservaRequestDTO reservaRequestDTO);

    void cancel(Long id);

    Page<ReservaResponseDTO> getReservasEntreFechas(LocalDate fechaDesde, LocalDate fechaHasta, int page, int size, String sortBy);

    ReservaResponseDTO getById(Long id);

    void empezarReserva(Long reservaId);
}
