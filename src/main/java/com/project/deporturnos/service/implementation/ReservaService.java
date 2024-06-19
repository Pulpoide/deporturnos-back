package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.*;
import com.project.deporturnos.entity.dto.ReservaRequestDTO;
import com.project.deporturnos.entity.dto.ReservaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.ReservaResponseDTO;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.exception.TurnoAlreadyReserved;
import com.project.deporturnos.repository.IReservaRepository;
import com.project.deporturnos.repository.ITurnoRepository;
import com.project.deporturnos.repository.IUsuarioRepository;
import com.project.deporturnos.service.IReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService implements IReservaService {

    @Autowired
    IReservaRepository reservaRepository;

    @Autowired
    IUsuarioRepository usuarioRepository;

    @Autowired
    ITurnoRepository turnoRepository;

    @Autowired
    ObjectMapper mapper;


    @Override
    public ReservaResponseDTO save(ReservaRequestDTO reservaRequestDTO) {

        assert reservaRequestDTO.getUsuarioId() != null;
        Usuario usuario = usuarioRepository.findById(reservaRequestDTO.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        Turno turno = turnoRepository.findById(reservaRequestDTO.getTurnoId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado."));

        Reserva reserva = mapper.convertValue(reservaRequestDTO, Reserva.class);

        reserva.setEstado(ReservaState.CONFIRMADA);
        reserva.setUsuario(usuario);
        reserva.setTurno(turno);

        if(turno.getEstado().equals(TurnoState.RESERVADO)){
            throw new TurnoAlreadyReserved("Turno no disponible.");
        }
        turno.setEstado(TurnoState.RESERVADO);

        Reserva reservaSaved = reservaRepository.save(reserva);
        return mapper.convertValue(reservaSaved, ReservaResponseDTO.class);
    }

    @Override
    public List<ReservaResponseDTO> getAll() {
        List<Reserva> reservas = reservaRepository.findAll();

        if(reservas.isEmpty()){
            throw new ResourceNotFoundException("No se encontraron reservas para listar.");
        }

        List<ReservaResponseDTO> reservaResponseDTOS = new ArrayList<>();
        for(Reserva reserva : reservas){
            reservaResponseDTOS.add(mapper.convertValue(reserva, ReservaResponseDTO.class));
        }

        return reservaResponseDTOS;
    }

    @Override
    public ReservaResponseDTO update(Long id, ReservaRequestUpdateDTO reservaRequestUpdateDTO) {
        Optional<Reserva> reservaOptional = reservaRepository.findById(id);

        if(reservaOptional.isEmpty()){
            throw new ResourceNotFoundException("Reserva no encontrada.");
        }

        Reserva reserva = reservaOptional.get();

        if(reservaRequestUpdateDTO.getFecha() != null){
            reserva.setFecha(reservaRequestUpdateDTO.getFecha());
        }
        if(reservaRequestUpdateDTO.getEstado() != null){
            reserva.setEstado(reservaRequestUpdateDTO.getEstado());
        }


        Reserva reservaUpdated = reservaRepository.save(reserva);
        return mapper.convertValue(reservaUpdated, ReservaResponseDTO.class);
    }

    @Override
    public void delete(Long id) {
        Optional<Reserva> reservaOptional = reservaRepository.findById(id);
        if(reservaOptional.isPresent()){
            reservaOptional.get().getTurno().setEstado(TurnoState.DISPONIBLE);
            reservaRepository.deleteById(id);
        }else{
            throw new ResourceNotFoundException("Reserva no encontrada.");
        }
    }

    @Override
    public ReservaResponseDTO saveReservaByUser(ReservaRequestDTO reservaRequestDTO) {

        Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Turno turno = turnoRepository.findById(reservaRequestDTO.getTurnoId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado."));

        if(turno.getEstado().equals(TurnoState.RESERVADO)){
            throw new TurnoAlreadyReserved("Turno no disponible.");
        }
        turno.setEstado(TurnoState.RESERVADO);

        Reserva reserva = mapper.convertValue(reservaRequestDTO, Reserva.class);

        reserva.setUsuario(currentUser);
        reserva.setTurno(turno);
        reserva.setEstado(ReservaState.CONFIRMADA);

        Reserva reservaSaved = reservaRepository.save(reserva);
        return mapper.convertValue(reservaSaved, ReservaResponseDTO.class);
    }
}
