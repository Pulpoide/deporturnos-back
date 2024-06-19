package com.project.deporturnos.service;

import com.project.deporturnos.entity.dto.ReservaRequestDTO;
import com.project.deporturnos.entity.dto.ReservaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.ReservaResponseDTO;

import java.util.List;

public interface IReservaService {
    ReservaResponseDTO save(ReservaRequestDTO reservaRequestDTO);

    List<ReservaResponseDTO> getAll();

    ReservaResponseDTO update(Long id, ReservaRequestUpdateDTO reservaRequestUpdateDTO);

    void delete(Long id);

    ReservaResponseDTO saveReservaByUser(ReservaRequestDTO reservaRequestDTO);
}
