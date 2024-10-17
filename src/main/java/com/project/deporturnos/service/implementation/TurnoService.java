package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.Cancha;
import com.project.deporturnos.entity.domain.Turno;
import com.project.deporturnos.entity.domain.TurnoState;
import com.project.deporturnos.entity.dto.CargaMasivaTurnosDTO;
import com.project.deporturnos.entity.dto.TurnoRequestDTO;
import com.project.deporturnos.entity.dto.TurnoRequestUpdateDTO;
import com.project.deporturnos.entity.dto.TurnoResponseDTO;
import com.project.deporturnos.exception.CanchaNotAvailableException;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.exception.TurnoStartTimeAlreadyExistException;
import com.project.deporturnos.repository.ICanchaRepository;
import com.project.deporturnos.repository.ITurnoRepository;
import com.project.deporturnos.repository.TurnoSpecification;
import com.project.deporturnos.service.ITurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TurnoService implements ITurnoService {

    private final ITurnoRepository turnoRepository;
    private final ICanchaRepository canchaRepository;
    private final ObjectMapper mapper;

    public TurnoResponseDTO save(TurnoRequestDTO turnoRequestDTO) {
        //Validamos que la cancha exista para crearle un turno
        Cancha cancha = canchaRepository.findById(turnoRequestDTO.getCanchaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada."));

        Turno turno = mapper.convertValue(turnoRequestDTO, Turno.class);

        // Validamos que no exista otro turno con la misma hora de inicio y la misma fecha para esa cancha
        for(Turno turno1 : cancha.getTurnos()){
            if(!turno1.isDeleted() && turno1.getHoraInicio().equals(turnoRequestDTO.getHoraInicio()) && turno1.getFecha().equals(turnoRequestDTO.getFecha())){
                throw new TurnoStartTimeAlreadyExistException("Ya existe el turno que está intentando crear.");
            }
        }

        if(turnoRequestDTO.getHoraInicio().equals(turnoRequestDTO.getHoraFin())){
            throw new TurnoStartTimeAlreadyExistException("La hora de inicio no puede ser igual a la hora de fin.");
        }

        turno.setFecha(turnoRequestDTO.getFecha());
        turno.setHoraInicio(turnoRequestDTO.getHoraInicio());
        turno.setHoraFin(turnoRequestDTO.getHoraFin());
        turno.setEstado(TurnoState.DISPONIBLE);

        if(cancha.isDisponibilidad()){
            turno.setCancha(cancha);
        }else{
            throw new CanchaNotAvailableException("La cancha no está disponible.");
        }



        Turno turnoSaved = turnoRepository.save(turno);
        return mapper.convertValue(turnoSaved, TurnoResponseDTO.class);
    }

    @Override
    public List<TurnoResponseDTO> getAll() {
        List<Turno> turnos = turnoRepository.findAllByDeletedFalse();

        if(turnos.isEmpty()){
            throw new ResourceNotFoundException("No se encontraron turnos para listar.");
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
            throw new ResourceNotFoundException("Turno no encontrado.");
        }

        Turno turno = turnoOptional.get();

        // Validamos que no exista otro turno con la misma hora de inicio para la cancha
        for(Turno turno1 : turno.getCancha().getTurnos()){
            if(!turno1.isDeleted() && turno1.getHoraInicio().equals(turnoRequestUpdateDTO.getHoraInicio())){
                throw new TurnoStartTimeAlreadyExistException("Ya existe un turno con esa hora de inicio para esta cancha.");
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

        if(turnoRequestUpdateDTO.getCanchaId() != null){
            Cancha cancha = canchaRepository.findById(turnoRequestUpdateDTO.getCanchaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada."));
            if(cancha.isDisponibilidad()) {
                turno.setCancha(cancha);
            }else{
                throw new CanchaNotAvailableException("Cancha no disponible.");
            }
        }

        Turno turnoUpdated = turnoRepository.save(turno);
        return mapper.convertValue(turnoUpdated, TurnoResponseDTO.class);
    }

    @Override
    public void delete(Long id) {
        Optional<Turno> turnoOptional = turnoRepository.findById(id);

        turnoOptional.ifPresent(turno -> {
            turno.setDeleted(true);
            turno.getReservas().forEach(reserva -> reserva.setDeleted(true));
            turnoRepository.save(turno);
        });

        if (turnoOptional.isEmpty()) {
        throw new ResourceNotFoundException("Turno no encontrado.");
        }
    }

    @Override
    public List<TurnoResponseDTO> getAllAvailableByCanchaAndDate(Long id, LocalDate fecha) {

        List<Turno> turnos = turnoRepository.findAll();

        if(turnos.isEmpty()){
            throw new ResourceNotFoundException("No se encontraron turnos para listar.");
        }

        List<TurnoResponseDTO> turnoAvailableResponseDTOS = new ArrayList<>();
        for(Turno turno : turnos){
            if(turno.getEstado().equals(TurnoState.DISPONIBLE)) {
                if(turno.getCancha().getId().equals(id)) {
                    if(turno.getFecha().equals(fecha)) {
                        turnoAvailableResponseDTOS.add(mapper.convertValue(turno, TurnoResponseDTO.class));
                    }
                }
            }
        }

        return turnoAvailableResponseDTOS;
    }

    @Transactional
    public int cargaMasivaTurnos(CargaMasivaTurnosDTO cargaMasivaTurnosDTO){
        LocalDate fechaActual = cargaMasivaTurnosDTO.getFechaDesde();
        int turnosCreados = 0;

        Cancha cancha = canchaRepository.findById(cargaMasivaTurnosDTO.getCanchaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada."));

        if(!cancha.isDisponibilidad()) {
            throw new CanchaNotAvailableException("Cancha no disponible.");
        }


        while (!fechaActual.isAfter(cargaMasivaTurnosDTO.getFechaHasta())) {
            LocalTime horaActual = cargaMasivaTurnosDTO.getHoraDesde();

            while (horaActual.isBefore(cargaMasivaTurnosDTO.getHoraHasta())) {
                boolean turnoExistente = turnoRepository.existsByCanchaAndFechaAndHoraInicio(
                        cancha, fechaActual, horaActual
                );

                if (turnoExistente) {
                    throw new TurnoStartTimeAlreadyExistException("Turno existente.");
                }else{
                    Turno nuevoTurno = new Turno();
                    nuevoTurno.setFecha(fechaActual);
                    nuevoTurno.setHoraInicio(horaActual);
                    nuevoTurno.setHoraFin(horaActual.plusMinutes(cargaMasivaTurnosDTO.getDuracionEnMinutos()));
                    nuevoTurno.setEstado(TurnoState.DISPONIBLE);
                    nuevoTurno.setCancha(cancha);
                    nuevoTurno.setDeleted(false);

                    turnosCreados++;
                    turnoRepository.save(nuevoTurno);
                }

                horaActual = horaActual.plusMinutes(cargaMasivaTurnosDTO.getDuracionEnMinutos());
            }

            fechaActual = fechaActual.plusDays(1);
        }
        return turnosCreados;
    }

    public List<TurnoResponseDTO> getTurnosEntreFechas(LocalDate fechaDesde, LocalDate fechaHasta) {
        LocalDate fechaDesdeDate = (fechaDesde != null) ? Date.valueOf(fechaDesde).toLocalDate() : null;
        LocalDate fechaHastaDate = (fechaHasta != null) ? Date.valueOf(fechaHasta).toLocalDate() : null;

        // Creamos la especificación del filtro
        TurnoSpecification specification = new TurnoSpecification(fechaDesdeDate, fechaHastaDate);

        // Buscamos los turnos usando la especificación
        List<Turno> turnos = turnoRepository.findAll(specification);

        if (turnos.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron turnos para listar en el rango de fechas.");
        }

        return turnos.stream()
                .map(turno -> mapper.convertValue(turno, TurnoResponseDTO.class))
                .collect(Collectors.toList());
    }

}
