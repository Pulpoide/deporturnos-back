package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.*;
import com.project.deporturnos.entity.dto.ReservaRequestDTO;
import com.project.deporturnos.entity.dto.ReservaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.ReservaResponseDTO;
import com.project.deporturnos.exception.*;
import com.project.deporturnos.repository.IReservaRepository;
import com.project.deporturnos.repository.ITurnoRepository;
import com.project.deporturnos.repository.IUsuarioRepository;
import com.project.deporturnos.repository.ReservaSpecification;
import com.project.deporturnos.service.IReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaService implements IReservaService {

    private final IReservaRepository reservaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final ITurnoRepository turnoRepository;
    private final ObjectMapper mapper;
    private final NotificationService notificationService;

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
        reserva.setFecha(LocalDate.now());

        if(turno.getEstado().equals(TurnoState.RESERVADO)){
            throw new TurnoAlreadyReservedException("Turno no disponible.");
        }
        turno.setEstado(TurnoState.RESERVADO);

        Reserva reservaSaved = reservaRepository.save(reserva);
        return mapper.convertValue(reservaSaved, ReservaResponseDTO.class);
    }

    @Override
    public List<ReservaResponseDTO> getAll() {
        List<Reserva> reservas = reservaRepository.findAllByDeletedFalse();

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

        //Manejo de estados
        if(reservaRequestUpdateDTO.getEstado() != null){

            ReservaState reservaState = reservaRequestUpdateDTO.getEstado();
            TurnoState turnoState = reserva.getTurno().getEstado();

            if(reservaState.equals(ReservaState.CANCELADA)){
                reserva.getTurno().setEstado(TurnoState.DISPONIBLE);

            } else if (reservaState.equals(ReservaState.CONFIRMADA)) {

                if(turnoState.equals(TurnoState.RESERVADO)){
                    throw new TurnoAlreadyReservedException("Turno no disponible.");
                }

                reserva.getTurno().setEstado(TurnoState.RESERVADO);

            } else if(reservaState.equals(ReservaState.COMPLETADA)){
                if(turnoState.equals(TurnoState.RESERVADO)){
                    reserva.getTurno().setEstado(TurnoState.COMPLETADO);
                }
            }

            reserva.setEstado(reservaRequestUpdateDTO.getEstado());
        }

        if(reservaRequestUpdateDTO.getTurnoId() != null) {
            Turno turno = turnoRepository.findById(reservaRequestUpdateDTO.getTurnoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado."));

            if(turno.getEstado().equals(TurnoState.RESERVADO)){
                throw new TurnoAlreadyReservedException("Turno no disponible.");
            }
            // El Turno anterior de esa reserva pasa a estar disponible
            reserva.getTurno().setEstado(TurnoState.DISPONIBLE);

            // El nuevo Turno pasa a estar reservado
            turno.setEstado(TurnoState.RESERVADO);

            reserva.setTurno(turno);
        }

        if(reservaRequestUpdateDTO.getUsuarioId() != null){
            Usuario usuario = usuarioRepository.findById(reservaRequestUpdateDTO.getUsuarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
            reserva.setUsuario(usuario);
        }

        if(reservaRequestUpdateDTO.getFecha() != null){
            reserva.setFecha(reservaRequestUpdateDTO.getFecha());
        }else{
            reserva.setFecha(LocalDate.now());
        }


        Reserva reservaUpdated = reservaRepository.save(reserva);
        return mapper.convertValue(reservaUpdated, ReservaResponseDTO.class);
    }

    @Override
    public void delete(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada."));

        reserva.getTurno().setEstado(TurnoState.DISPONIBLE);
        reserva.setDeleted(true);
        reservaRepository.save(reserva);
    }

    @Override
    public ReservaResponseDTO saveReservaByUser(ReservaRequestDTO reservaRequestDTO) {

        Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Turno turno = turnoRepository.findById(reservaRequestDTO.getTurnoId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado."));

        if(turno.getEstado().equals(TurnoState.RESERVADO)){
            throw new TurnoAlreadyReservedException("Turno no disponible.");
        }
        turno.setEstado(TurnoState.RESERVADO);

        Reserva reserva = mapper.convertValue(reservaRequestDTO, Reserva.class);

        reserva.setUsuario(currentUser);
        reserva.setTurno(turno);
        reserva.setEstado(ReservaState.CONFIRMADA);
        reserva.setFecha(LocalDate.now());


        Reserva reservaSaved = reservaRepository.save(reserva);

        notificationService.sendNotificationReservationConfirmed(currentUser, reservaSaved.getId());

        return mapper.convertValue(reservaSaved, ReservaResponseDTO.class);
    }

    @Override
    public void cancel(Long id) {
        Optional<Reserva> reservaOptional = reservaRepository.findById(id);
        Usuario currentUser = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(reservaOptional.isPresent()){
            Reserva reservaCancel = reservaOptional.get();
            Turno turno = reservaCancel.getTurno();
            LocalDate now = LocalDate.now();

            if(reservaCancel.getEstado().equals(ReservaState.CANCELADA)){
                throw new ReservaAlreadyCancelledException("La reserva ya se encuentra cancelada.");
            }

            if(currentUser.getRol().equals(Rol.CLIENTE)) {
                if (!Objects.equals(reservaCancel.getUsuario().getId(), currentUser.getId())) {
                    throw new InsufficientAuthenticationException("No autorizado.");
                }
            }

            turno.setEstado(TurnoState.DISPONIBLE);

            if(Objects.equals(turno.getFecha(), now) || Objects.equals(turno.getFecha(), now.plusDays(1))){
                //List<Usuario> usuariosANotificar = usuarioRepository.findByDeletedFalseAndRolAndNotificacionesTrue(Rol.CLIENTE);

                // Enviar email a usuarios notification=true ->
                //notificationService.notifyUsersAboutPromotions(usuariosANotificar, turno, reservaCancel.getUsuario());
            }

            reservaCancel.setEstado(ReservaState.CANCELADA);
            reservaRepository.save(reservaCancel);
        }else{
            throw new ResourceNotFoundException("Reserva no encontrada.");
        }
    }

    public List<ReservaResponseDTO> getReservasEntreFechas(LocalDate fechaDesde, LocalDate fechaHasta) {
        LocalDate fechaDesdeDate = (fechaDesde != null) ? Date.valueOf(fechaDesde).toLocalDate() : null;
        LocalDate fechaHastaDate = (fechaHasta != null) ? Date.valueOf(fechaHasta).toLocalDate() : null;

        // Especificación de la reserva
        ReservaSpecification specification = new ReservaSpecification(fechaDesdeDate, fechaHastaDate);

        // Buscamos las reservas usando la especificación
        List<Reserva> reservas = reservaRepository.findAll(specification);

        if(reservas.isEmpty()){
            throw new ResourceNotFoundException("No se encontraron reservas para listar en el rango de fechas proporcionado.");
        }

        return reservas.stream()
                .map(reserva -> mapper.convertValue(reserva, ReservaResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ReservaResponseDTO getById(Long id) {
        Optional<Reserva> reservaOptional = reservaRepository.findById(id);
        if(reservaOptional.isEmpty() || reservaOptional.get().isDeleted()){
            throw new ResourceNotFoundException("Reserva no encontrada.");
        }
        return mapper.convertValue(reservaOptional, ReservaResponseDTO.class);
    }

    @Override
    public void empezarReserva(Long reservaId) {
        Optional<Reserva> reservaOptional = reservaRepository.findById(reservaId);

        if(reservaOptional.isEmpty() || reservaOptional.get().isDeleted()){
            throw new ResourceNotFoundException("Reserva no encontrada.");
        }

        if(reservaOptional.get().getEstado().equals(ReservaState.CANCELADA)){
            throw new ReservaAlreadyCancelledException("La reserva se encuentra cancelada. No se puede iniciar una reserva cancelada.");
        }

        if(reservaOptional.get().getEstado().equals(ReservaState.EN_PROCESO)){
            throw new ReservaAlreadyInProcessException("La reserva ya se encuentra en proceso.");
        }

        if(reservaOptional.get().getEstado().equals(ReservaState.COMPLETADA)){
            throw new ReservaAlreadyCompletedException("La reserva ya se encuentra completada.");
        }

        if(!Objects.equals(reservaOptional.get().getFecha(), LocalDate.now())){
            throw new InvalidReservaDateException("La fecha de la reserva no coincide con la fecha de hoy.");
        }

        reservaOptional.get().setEstado(ReservaState.EN_PROCESO);
        reservaRepository.save(reservaOptional.get());
    }
}
