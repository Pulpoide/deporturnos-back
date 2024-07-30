package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.*;
import com.project.deporturnos.entity.dto.ReservaRequestDTO;
import com.project.deporturnos.entity.dto.ReservaRequestUpdateByUserDTO;
import com.project.deporturnos.entity.dto.ReservaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.ReservaResponseDTO;
import com.project.deporturnos.exception.ReservaAlreadyCancelledException;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.exception.TurnoAlreadyReservedException;
import com.project.deporturnos.repository.IReservaRepository;
import com.project.deporturnos.repository.ITurnoRepository;
import com.project.deporturnos.repository.IUsuarioRepository;
import com.project.deporturnos.service.IReservaService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Turno turno = turnoRepository.findById(reservaRequestDTO.getTurnoId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));

        Reserva reserva = mapper.convertValue(reservaRequestDTO, Reserva.class);

        reserva.setEstado(ReservaState.CONFIRMADA);
        reserva.setUsuario(usuario);
        reserva.setTurno(turno);
        reserva.setFecha(LocalDate.now());

        if(turno.getEstado().equals(TurnoState.RESERVADO)){
            throw new TurnoAlreadyReservedException("Turno no disponible");
        }
        turno.setEstado(TurnoState.RESERVADO);

        Reserva reservaSaved = reservaRepository.save(reserva);
        return mapper.convertValue(reservaSaved, ReservaResponseDTO.class);
    }

    @Override
    public List<ReservaResponseDTO> getAll() {
        List<Reserva> reservas = reservaRepository.findAllByDeletedFalse();

        if(reservas.isEmpty()){
            throw new ResourceNotFoundException("No se encontraron reservas para listar");
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
            throw new ResourceNotFoundException("Reserva no encontrada");
        }

        Reserva reserva = reservaOptional.get();

        //Manejo de estados
        if(reservaRequestUpdateDTO.getEstado() != null){
            ReservaState reservaState = reservaRequestUpdateDTO.getEstado();
            if(reservaState.equals(ReservaState.CANCELADA)){
                reserva.getTurno().setEstado(TurnoState.DISPONIBLE);
            } else if (reservaState.equals(ReservaState.CONFIRMADA)) {
                reserva.getTurno().setEstado(TurnoState.RESERVADO);
            }
            reserva.setEstado(reservaRequestUpdateDTO.getEstado());
        }

        if(reservaRequestUpdateDTO.getTurnoId()!=null) {
            Turno turno = turnoRepository.findById(reservaRequestUpdateDTO.getTurnoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));

            if(turno.getEstado().equals(TurnoState.RESERVADO)){
                throw new TurnoAlreadyReservedException("Turno no disponibles");
            }
            reserva.setTurno(turno);
            turno.setEstado(TurnoState.RESERVADO);
        }

        if(reservaRequestUpdateDTO.getUsuarioId()!=null){
            Usuario usuario = usuarioRepository.findById(reservaRequestUpdateDTO.getUsuarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            reserva.setUsuario(usuario);
        }

        reserva.setFecha(LocalDate.now());


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
            throw new ResourceNotFoundException("Reserva no encontrada");
        }
    }

    @Override
    public ReservaResponseDTO saveReservaByUser(ReservaRequestDTO reservaRequestDTO) {

        Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Turno turno = turnoRepository.findById(reservaRequestDTO.getTurnoId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));

        if(turno.getEstado().equals(TurnoState.RESERVADO)){
            throw new TurnoAlreadyReservedException("Turno no disponible");
        }
        turno.setEstado(TurnoState.RESERVADO);

        Reserva reserva = mapper.convertValue(reservaRequestDTO, Reserva.class);

        reserva.setUsuario(currentUser);
        reserva.setTurno(turno);
        reserva.setEstado(ReservaState.CONFIRMADA);
        reserva.setFecha(LocalDate.now());

        Reserva reservaSaved = reservaRepository.save(reserva);
        return mapper.convertValue(reservaSaved, ReservaResponseDTO.class);
    }

    @Override
    public void cancel(Long id) {
        Optional<Reserva> reservaOptional = reservaRepository.findById(id);
        Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        if(reservaOptional.isPresent()){
            Reserva reserva = reservaOptional.get();

            if(reserva.getEstado().equals(ReservaState.CANCELADA)){
                throw new ReservaAlreadyCancelledException("La reserva ya se encuentra cancelada");
            }

            if(currentUser.getRol().equals(Rol.CLIENTE)) {
                if (!Objects.equals(reserva.getUsuario().getId(), currentUser.getId())) {
                    throw new InsufficientAuthenticationException("No autorizado");
                }
            }

            reserva.getTurno().setEstado(TurnoState.DISPONIBLE);
            reserva.setEstado(ReservaState.CANCELADA);
            reservaRepository.save(reserva);
        }else{
            throw new ResourceNotFoundException("Reserva no encontrada");
        }
    }

    @Override
    public ReservaResponseDTO updateReservaByUser(Long id, @Valid ReservaRequestUpdateByUserDTO reservaRequestUpdateByUserDTO) {
        Optional<Reserva> reservaOptional = reservaRepository.findById(id);
        Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        Turno turno = turnoRepository.findById(reservaRequestUpdateByUserDTO.getTurnoId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));

        if(reservaOptional.isEmpty()){
            throw new ResourceNotFoundException("Reserva no encontrada");
        }

        Reserva reserva = reservaOptional.get();

        if(currentUser.getRol().equals(Rol.CLIENTE)) {
            if (!Objects.equals(reserva.getUsuario().getId(), currentUser.getId())) {
                throw new InsufficientAuthenticationException("No autorizado");
            }
        }

        reserva.getTurno().setEstado(TurnoState.DISPONIBLE);

        if(turno.getEstado().equals(TurnoState.RESERVADO)){
            throw new TurnoAlreadyReservedException("Turno no disponible");
        }

        reserva.setTurno(turno);
        turno.setEstado(TurnoState.RESERVADO);
        reserva.setFecha(LocalDate.now());
        reserva.setEstado(ReservaState.MODIFICADA);

        Reserva reservaUpdated = reservaRepository.save(reserva);
        return mapper.convertValue(reservaUpdated, ReservaResponseDTO.class);
    }
}
