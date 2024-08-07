package com.project.deporturnos.service;

import com.project.deporturnos.entity.dto.CanchaRequestDTO;
import com.project.deporturnos.entity.dto.CanchaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.CanchaResponseDTO;

import java.util.List;

public interface ICanchaService {

    CanchaResponseDTO save(CanchaRequestDTO canchaRequestDTO);

    List<CanchaResponseDTO> getAll();

    CanchaResponseDTO update(Long id, CanchaRequestUpdateDTO canchaRequestUpdateDTO);

    void delete(Long id);

    List<CanchaResponseDTO> getAvailableByDeporte(String deporte);

}
