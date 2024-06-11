package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.Cancha;
import com.project.deporturnos.entity.domain.Turno;
import com.project.deporturnos.entity.domain.TurnoState;
import com.project.deporturnos.entity.dto.TurnoRequestDTO;
import com.project.deporturnos.entity.dto.TurnoResponseDTO;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.repository.ICanchaRepository;
import com.project.deporturnos.repository.ITurnoRepository;
import com.project.deporturnos.service.ITurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TurnoService implements ITurnoService {

    @Autowired
    ITurnoRepository turnoRepository;

    @Autowired
    private ICanchaRepository canchaRepository;

    @Autowired
    ObjectMapper mapper;

    public TurnoResponseDTO save(TurnoRequestDTO turnoRequestDTO) {

        //Validarmos que la cancha exista
        Cancha cancha = canchaRepository.findById(turnoRequestDTO.getCanchaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada."));


        Turno turno = mapper.convertValue(turnoRequestDTO, Turno.class);

        turno.setFecha(turnoRequestDTO.getFecha());
        turno.setHoraInicio(turnoRequestDTO.getHoraInicio());
        turno.setHoraFin(turnoRequestDTO.getHoraFin());
        turno.setEstado(TurnoState.DISPONIBLE);
        turno.setCancha(cancha);


        Turno turnoSaved = turnoRepository.save(turno);
        return mapper.convertValue(turnoSaved, TurnoResponseDTO.class);
    }
}
