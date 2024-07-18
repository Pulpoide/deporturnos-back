package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.Cancha;
import com.project.deporturnos.entity.domain.Turno;
import com.project.deporturnos.entity.domain.TurnoState;
import com.project.deporturnos.entity.dto.TurnoRequestDTO;
import com.project.deporturnos.entity.dto.TurnoRequestUpdateDTO;
import com.project.deporturnos.entity.dto.TurnoResponseDTO;
import com.project.deporturnos.exception.CanchaNotAvailableException;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.exception.TurnoStartTimeAlreadyExistException;
import com.project.deporturnos.repository.ICanchaRepository;
import com.project.deporturnos.repository.ITurnoRepository;
import com.project.deporturnos.service.ITurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class TurnoService implements ITurnoService {

    @Autowired
    ITurnoRepository turnoRepository;

    @Autowired
    ICanchaRepository canchaRepository;

    @Autowired
    ObjectMapper mapper;

    public TurnoResponseDTO save(TurnoRequestDTO turnoRequestDTO) {

        //Validamos que la cancha exista para crearle un turno
        Cancha cancha = canchaRepository.findById(turnoRequestDTO.getCanchaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada"));

        Turno turno = mapper.convertValue(turnoRequestDTO, Turno.class);

        // Validamos que no exista otro turno con la misma hora de inicio para la cancha
        for(Turno turno1 : cancha.getTurnos()){
            if(turno1.getHoraInicio().equals(turnoRequestDTO.getHoraInicio())){
                throw new TurnoStartTimeAlreadyExistException("Ya existe un turno con esa hora de inicio para esta cancha");
            }
        }

        if(turnoRequestDTO.getHoraInicio().equals(turnoRequestDTO.getHoraFin())){
            throw new TurnoStartTimeAlreadyExistException("La hora de inicio no puede ser igual a la hora de fin");
        }

        turno.setFecha(turnoRequestDTO.getFecha());
        turno.setHoraInicio(turnoRequestDTO.getHoraInicio());
        turno.setHoraFin(turnoRequestDTO.getHoraFin());
        turno.setEstado(TurnoState.DISPONIBLE);

        if(cancha.isDisponibilidad()){
            turno.setCancha(cancha);
        }else{
            throw new CanchaNotAvailableException("La cancha no está disponible");
        }



        Turno turnoSaved = turnoRepository.save(turno);
        return mapper.convertValue(turnoSaved, TurnoResponseDTO.class);
    }

    @Override
    public List<TurnoResponseDTO> getAll() {
        List<Turno> turnos = turnoRepository.findAll();

        if(turnos.isEmpty()){
            throw new ResourceNotFoundException("No se encontraron turnos para listar");
        }

        List<TurnoResponseDTO> turnoResponseDTOS = new ArrayList<>();
        for(Turno turno : turnos){
            turnoResponseDTOS.add(mapper.convertValue(turno, TurnoResponseDTO.class));
        }

        return turnoResponseDTOS;
    }

    @Override
    public TurnoResponseDTO update(Long id, TurnoRequestUpdateDTO turnoRequestUpdateDTO) {
        Optional<Turno> turnoOptional = turnoRepository.findById(id);

        if(turnoOptional.isEmpty()){
            throw new ResourceNotFoundException("Turno no encontrado");
        }

        Turno turno = turnoOptional.get();

        // Validamos que no exista otro turno con la misma hora de inicio para la cancha
        for(Turno turno1 : turno.getCancha().getTurnos()){
            if(turno1.getHoraInicio().equals(turnoRequestUpdateDTO.getHoraInicio())){
                throw new TurnoStartTimeAlreadyExistException("Ya existe un turno con esa hora de inicio para esta cancha");
            }
        }


        if(turnoRequestUpdateDTO.getFecha() != null){
            turno.setFecha(turnoRequestUpdateDTO.getFecha());
        }

        if(turnoRequestUpdateDTO.getHoraInicio() != null){
            turno.setHoraInicio(turnoRequestUpdateDTO.getHoraInicio());
        }

        if(turnoRequestUpdateDTO.getHoraFin() != null){
            turno.setHoraFin(turnoRequestUpdateDTO.getHoraFin());
        }

        if(turnoRequestUpdateDTO.getEstado() != null){
            turno.setEstado(turnoRequestUpdateDTO.getEstado());
        }

        Turno turnoUpdated = turnoRepository.save(turno);
        return mapper.convertValue(turnoUpdated, TurnoResponseDTO.class);
    }

    @Override
    public void delete(Long id) {
        Optional<Turno> turnoOptional = turnoRepository.findById(id);
        if(turnoOptional.isPresent()){
            turnoRepository.deleteById(id);
        }else{
            throw new ResourceNotFoundException("Turno no encontrado");
        }
    }

    @Override
    public List<TurnoResponseDTO> getAllAvailable(Long id) {

        List<Turno> turnos = turnoRepository.findAll();

        if(turnos.isEmpty()){
            throw new ResourceNotFoundException("No se encontraron turnos para listar");
        }

        List<TurnoResponseDTO> turnoAvailableResponseDTOS = new ArrayList<>();
        for(Turno turno : turnos){
            if(turno.getEstado().equals(TurnoState.DISPONIBLE)) {
                if(turno.getCancha().getId().equals(id)) {
                    turnoAvailableResponseDTOS.add(mapper.convertValue(turno, TurnoResponseDTO.class));
                }
            }
        }

        return turnoAvailableResponseDTOS;
    }

}
