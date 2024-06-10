package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.Cancha;
import com.project.deporturnos.entity.dto.CanchaRequestDTO;
import com.project.deporturnos.entity.dto.CanchaResponseDTO;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.repository.ICanchaRepository;
import com.project.deporturnos.service.ICanchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CanchaService implements ICanchaService {

    @Autowired
    ICanchaRepository canchaRepository;

    @Autowired
    ObjectMapper mapper;

    @Override
    public CanchaResponseDTO save(CanchaRequestDTO canchaRequestDTO){

        Cancha cancha = mapper.convertValue(canchaRequestDTO, Cancha.class);
        cancha.setDisponibilidad(true);

        Cancha canchaGuardada = canchaRepository.save(cancha);
        return mapper.convertValue(canchaGuardada, CanchaResponseDTO.class);
    }


    @Override
    public List<CanchaResponseDTO> getAll(){
        List<Cancha> canchas = canchaRepository.findAll();

        if(canchas.isEmpty()){
            throw new ResourceNotFoundException("No se encontraron canchas para listar.");
        }

        List<CanchaResponseDTO> canchaResponseDTOS = new ArrayList<>();
        for(Cancha cancha : canchas){
            canchaResponseDTOS.add(mapper.convertValue(cancha, CanchaResponseDTO.class));
        }

        return canchaResponseDTOS;
    }
}
