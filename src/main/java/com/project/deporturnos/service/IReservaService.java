package com.project.deporturnos.service;

import com.project.deporturnos.entity.dto.ReservaRequestDTO;
import com.project.deporturnos.entity.dto.ReservaRequestUpdateByUserDTO;
import com.project.deporturnos.entity.dto.ReservaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.ReservaResponseDTO;

import jakarta.validation.Valid;

import java.util.List;

public interface IReservaService {
    ReservaResponseDTO save(ReservaRequestDTO reservaRequestDTO);

    List<ReservaResponseDTO> getAll();

    ReservaResponseDTO update(Long id, ReservaRequestUpdateDTO reservaRequestUpdateDTO);

    void delete(Long id);

    ReservaResponseDTO saveReservaByUser(ReservaRequestDTO reservaRequestDTO);

    void cancel(Long id);

    ReservaResponseDTO updateReservaByUser(Long id, @Valid ReservaRequestUpdateByUserDTO reservaRequestUpdateByUserDTO);
}
