package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.Cancha;
import com.project.deporturnos.entity.dto.CanchaRequestDTO;
import com.project.deporturnos.entity.dto.CanchaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.CanchaResponseDTO;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.repository.ICanchaRepository;
import com.project.deporturnos.service.ICanchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        Cancha canchaSaved = canchaRepository.save(cancha);
        return mapper.convertValue(canchaSaved, CanchaResponseDTO.class);
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


    @Override
    public CanchaResponseDTO update(Long id, CanchaRequestUpdateDTO canchaRequestUpdateDTO){
        Optional<Cancha> canchaOptional = canchaRepository.findById(id);

        if(canchaOptional.isEmpty()){
            throw new ResourceNotFoundException("No se encontró la cancha con id " + id + ".");
        }

        Cancha cancha = canchaOptional.get();

        if(canchaRequestUpdateDTO.getNombre() != null){
            cancha.setNombre(canchaRequestUpdateDTO.getNombre());
        }

        if(canchaRequestUpdateDTO.getTipo() != null){
            cancha.setTipo(canchaRequestUpdateDTO.getTipo());
        }


        cancha.setPrecioHora(canchaRequestUpdateDTO.getPrecioHora());
        cancha.setDisponibilidad(canchaRequestUpdateDTO.isDisponibilidad());


        if(canchaRequestUpdateDTO.getDescripcion() != null){
            cancha.setDescripcion(canchaRequestUpdateDTO.getDescripcion());
        }

        Cancha canchaUpdated = canchaRepository.save(cancha);
        return mapper.convertValue(canchaUpdated, CanchaResponseDTO.class);
    }


    @Override
    public void delete(Long id){
        Optional<Cancha> canchaOptional = canchaRepository.findById(id);
        if(canchaOptional.isPresent()){
            canchaRepository.deleteById(id);
        }else{
            throw new ResourceNotFoundException("No existe la cancha con el id "+id);
        }
    }


}
