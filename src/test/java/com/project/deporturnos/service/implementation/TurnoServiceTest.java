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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

    @InjectMocks
    private TurnoService turnoService;

    @Mock
    private ITurnoRepository turnoRepository;

    @Mock
    private ICanchaRepository canchaRepository;

    @Mock
    private ObjectMapper mapper;


    /* Metodo save() */
    @Test
    void save_Success() {
        TurnoRequestDTO turnoRequestDTO = new TurnoRequestDTO();
        turnoRequestDTO.setCanchaId(1L);
        turnoRequestDTO.setFecha(LocalDate.now());
        turnoRequestDTO.setHoraInicio(LocalTime.of(11, 0));
        turnoRequestDTO.setHoraFin(LocalTime.of(12, 0));
        turnoRequestDTO.setEstado(TurnoState.DISPONIBLE);

        Cancha cancha = new Cancha();
        cancha.setId(1L);
        cancha.setDisponibilidad(true);
        cancha.setTurnos(new HashSet<>());

        Turno turno = new Turno();
        turno.setId(1L);
        turno.setFecha(turnoRequestDTO.getFecha());
        turno.setHoraInicio(turnoRequestDTO.getHoraInicio());
        turno.setHoraFin(turnoRequestDTO.getHoraFin());
        turno.setCancha(cancha);

        TurnoResponseDTO turnoResponseDTO = new TurnoResponseDTO();
        turnoResponseDTO.setId(1L);
        turnoResponseDTO.setFecha(turnoRequestDTO.getFecha());
        turnoResponseDTO.setHoraInicio(turnoRequestDTO.getHoraInicio());
        turnoResponseDTO.setHoraFin(turnoRequestDTO.getHoraFin());
        turnoResponseDTO.setEstado(turnoRequestDTO.getEstado());
        turnoResponseDTO.setCancha(cancha);


        when(canchaRepository.findById(1L)).thenReturn(Optional.of(cancha));
        when(turnoRepository.save(any(Turno.class))).thenReturn(turno);
        when(mapper.convertValue(turnoRequestDTO, Turno.class)).thenReturn(turno);
        when(mapper.convertValue(any(Turno.class), eq(TurnoResponseDTO.class)))
                .thenReturn(turnoResponseDTO);

        TurnoResponseDTO result = turnoService.save(turnoRequestDTO);

        assertNotNull(result);
        assertEquals(result, turnoResponseDTO);

        verify(turnoRepository).save(any(Turno.class));
    }

    @Test
    void save_NotFound() {
        TurnoRequestDTO turnoRequestDTO = new TurnoRequestDTO();
        turnoRequestDTO.setCanchaId(66L);

        when(canchaRepository.findById(66L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> turnoService.save(turnoRequestDTO));

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void save_AlreadyExists() {
        TurnoRequestDTO turnoRequestDTO = new TurnoRequestDTO();
        turnoRequestDTO.setCanchaId(1L);
        turnoRequestDTO.setFecha(LocalDate.now());
        turnoRequestDTO.setHoraInicio(LocalTime.of(10, 0));

        Cancha cancha = new Cancha();
        cancha.setId(1L);
        cancha.setDisponibilidad(true);

        Turno existingTurno = new Turno();
        existingTurno.setHoraInicio(LocalTime.of(10, 0));
        existingTurno.setFecha(LocalDate.now());
        existingTurno.setDeleted(false);

        cancha.setTurnos(new HashSet<>(Collections.singletonList(existingTurno)));

        when(canchaRepository.findById(1L)).thenReturn(Optional.of(cancha));

        assertThrows(TurnoStartTimeAlreadyExistException.class, () -> turnoService.save(turnoRequestDTO));

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void save_CanchaNotAvailable() {
        TurnoRequestDTO turnoRequestDTO = new TurnoRequestDTO();
        turnoRequestDTO.setCanchaId(1L);
        turnoRequestDTO.setFecha(LocalDate.now());
        turnoRequestDTO.setHoraInicio(LocalTime.of(11, 0));
        turnoRequestDTO.setHoraFin(LocalTime.of(12, 0));

        Cancha cancha = new Cancha();
        cancha.setId(1L);
        cancha.setDisponibilidad(false); // Cancha no disponible
        cancha.setTurnos(new HashSet<>());

        Turno turno = new Turno();
        turno.setId(1L);
        turno.setFecha(turnoRequestDTO.getFecha());
        turno.setHoraInicio(turnoRequestDTO.getHoraInicio());
        turno.setHoraFin(turnoRequestDTO.getHoraFin());
        turno.setCancha(cancha);

        when(canchaRepository.findById(1L)).thenReturn(Optional.of(cancha));
        when(mapper.convertValue(turnoRequestDTO, Turno.class)).thenReturn(turno);

        assertThrows(CanchaNotAvailableException.class, () ->
                turnoService.save(turnoRequestDTO));

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    /* Metodo getAll() */
    @Test
    void getAll_Success() {
        List<Turno> turnos = List.of(new Turno());
        TurnoResponseDTO expectedResponse = new TurnoResponseDTO();

        when(turnoRepository.findAllByDeletedFalse()).thenReturn(turnos);
        when(mapper.convertValue(any(Turno.class), eq(TurnoResponseDTO.class))).thenReturn(expectedResponse);

        List<TurnoResponseDTO> actualResponse = turnoService.getAll();

        assertNotNull(actualResponse);
        assertEquals(1, actualResponse.size());
        assertEquals(expectedResponse, actualResponse.get(0));
    }

    @Test
    void getAll_NotFound() {
        when(turnoRepository.findAllByDeletedFalse()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> turnoService.getAll());
    }

    /* Metodo update() */
    @Test
    void update_Success() {
        TurnoRequestUpdateDTO turnoRequestUpdateDTO = new TurnoRequestUpdateDTO();
        turnoRequestUpdateDTO.setHoraInicio(LocalTime.of(15, 0));
        turnoRequestUpdateDTO.setHoraFin(LocalTime.of(16, 0));
        turnoRequestUpdateDTO.setFecha(LocalDate.now());

        Turno turno = new Turno();
        turno.setId(1L);
        turno.setFecha(LocalDate.now());
        turno.setHoraInicio(LocalTime.of(11, 0));
        turno.setHoraFin(LocalTime.of(12, 0));
        turno.setEstado(TurnoState.DISPONIBLE);

        Cancha cancha = new Cancha();
        cancha.setId(1L);
        cancha.setDisponibilidad(true);
        cancha.setTurnos(new HashSet<>());
        turno.setCancha(cancha);

        TurnoResponseDTO turnoResponseDTO = new TurnoResponseDTO();
        turnoResponseDTO.setId(1L);
        turnoResponseDTO.setHoraInicio(turnoRequestUpdateDTO.getHoraInicio());
        turnoResponseDTO.setHoraFin(turnoRequestUpdateDTO.getHoraFin());

        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
        when(turnoRepository.save(any(Turno.class))).thenReturn(turno);
        when(mapper.convertValue(turno, TurnoResponseDTO.class)).thenReturn(turnoResponseDTO);

        TurnoResponseDTO result = turnoService.update(1L, turnoRequestUpdateDTO);

        assertNotNull(result);
        assertEquals(turnoRequestUpdateDTO.getHoraInicio(), result.getHoraInicio());
        assertEquals(turnoRequestUpdateDTO.getHoraFin(), result.getHoraFin());

        verify(turnoRepository).save(turno);
    }

    @Test
    void update_NotFound() {
        TurnoRequestUpdateDTO turnoRequestUpdateDTO = new TurnoRequestUpdateDTO();
        turnoRequestUpdateDTO.setHoraInicio(LocalTime.of(15, 0));
        turnoRequestUpdateDTO.setHoraFin(LocalTime.of(16, 0));

        when(turnoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> turnoService.update(1L, turnoRequestUpdateDTO));

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void update_AlreadyExist() {
        TurnoRequestUpdateDTO turnoRequestUpdateDTO = new TurnoRequestUpdateDTO();
        turnoRequestUpdateDTO.setHoraInicio(LocalTime.of(14, 0));

        Turno turno = new Turno();
        turno.setId(1L);
        turno.setHoraInicio(LocalTime.of(10, 0));
        turno.setCancha(new Cancha());

        Turno conflictingTurno = new Turno();
        conflictingTurno.setHoraInicio(LocalTime.of(14, 0));
        conflictingTurno.setDeleted(false);

        turno.getCancha().setTurnos(Set.of(conflictingTurno));

        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));

        assertThrows(TurnoStartTimeAlreadyExistException.class, () -> turnoService.update(1L, turnoRequestUpdateDTO));

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void update_CanchaNotFound() {
        TurnoRequestUpdateDTO turnoRequestUpdateDTO = new TurnoRequestUpdateDTO();
        turnoRequestUpdateDTO.setCanchaId(2L);
        turnoRequestUpdateDTO.setHoraInicio(LocalTime.of(15, 0));
        turnoRequestUpdateDTO.setHoraFin(LocalTime.of(16, 0));

        Turno turno = new Turno();
        turno.setId(1L);
        turno.setFecha(LocalDate.now());
        turno.setHoraInicio(LocalTime.of(11, 0));
        turno.setHoraFin(LocalTime.of(12, 0));

        Cancha cancha = new Cancha();
        cancha.setId(1L);
        cancha.setDisponibilidad(true);
        cancha.setTurnos(new HashSet<>());
        turno.setCancha(cancha);

        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
        when(canchaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> turnoService.update(1L, turnoRequestUpdateDTO));

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void update_CanchaNotAvailable() {
        TurnoRequestUpdateDTO turnoRequestUpdateDTO = new TurnoRequestUpdateDTO();
        turnoRequestUpdateDTO.setCanchaId(2L);

        Turno turno = new Turno();
        turno.setId(1L);
        turno.setFecha(LocalDate.now());
        turno.setHoraInicio(LocalTime.of(11, 0));
        turno.setHoraFin(LocalTime.of(12, 0));

        Cancha cancha = new Cancha();
        cancha.setId(1L);
        cancha.setDisponibilidad(true);
        cancha.setTurnos(new HashSet<>());
        turno.setCancha(cancha);

        Cancha unavailableCancha = new Cancha();
        unavailableCancha.setId(2L);
        unavailableCancha.setDisponibilidad(false);

        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
        when(canchaRepository.findById(2L)).thenReturn(Optional.of(unavailableCancha));

        assertThrows(CanchaNotAvailableException.class, () -> turnoService.update(1L, turnoRequestUpdateDTO));

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    /* Metodo delete() */
    @Test
    void delete_Success() {
        Turno turno = new Turno();
        turno.setId(1L);
        turno.setFecha(LocalDate.now());
        turno.setHoraInicio(LocalTime.of(11, 0));
        turno.setHoraFin(LocalTime.of(12, 0));
        turno.setDeleted(false);
        turno.setReservas(new HashSet<>());

        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));

        turnoService.delete(1L);

        assertTrue(turno.isDeleted());
        verify(turnoRepository).save(turno);
    }

    @Test
    void delete_NotFound() {
        when(turnoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> turnoService.delete(1L));

        verify(turnoRepository, never()).save(any(Turno.class));
    }

    /* Metodo getAllAvailableByCanchaAndDate()*/
    @Test
    void getAllAvailableByCanchaAndDate_Success() {
        LocalDate fecha = LocalDate.now();

        Cancha cancha = new Cancha();
        cancha.setId(1L);

        Turno turno1 = new Turno();
        turno1.setId(1L);
        turno1.setFecha(fecha);
        turno1.setCancha(cancha);
        turno1.setEstado(TurnoState.DISPONIBLE);
        turno1.setDeleted(false);

        Turno turno2 = new Turno();
        turno2.setId(2L);
        turno2.setFecha(fecha);
        turno2.setCancha(cancha);
        turno2.setEstado(TurnoState.DISPONIBLE);
        turno2.setDeleted(false);

        List<Turno> turnos = List.of(turno1, turno2);

        TurnoResponseDTO turnoResponseDTO1 = new TurnoResponseDTO();
        turnoResponseDTO1.setId(1L);
        turnoResponseDTO1.setFecha(fecha);
        turnoResponseDTO1.setHoraInicio(turno1.getHoraInicio());
        turnoResponseDTO1.setHoraFin(turno1.getHoraFin());

        TurnoResponseDTO turnoResponseDTO2 = new TurnoResponseDTO();
        turnoResponseDTO2.setId(2L);
        turnoResponseDTO2.setFecha(fecha);
        turnoResponseDTO2.setHoraInicio(turno2.getHoraInicio());
        turnoResponseDTO2.setHoraFin(turno2.getHoraFin());

        when(turnoRepository.findAll()).thenReturn(turnos);
        when(mapper.convertValue(turno1, TurnoResponseDTO.class)).thenReturn(turnoResponseDTO1);
        when(mapper.convertValue(turno2, TurnoResponseDTO.class)).thenReturn(turnoResponseDTO2);

        List<TurnoResponseDTO> result = turnoService.getAllAvailableByCanchaAndDate(1L, fecha);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(turnoResponseDTO1, result.get(0));
        assertEquals(turnoResponseDTO2, result.get(1));

        verify(turnoRepository).findAll();
        verify(mapper).convertValue(turno1, TurnoResponseDTO.class);
        verify(mapper).convertValue(turno2, TurnoResponseDTO.class);
    }

    @Test
    void getAllAvailableByCanchaAndDate_NotFound() {
        LocalDate fecha = LocalDate.now();

        when(turnoRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> turnoService.getAllAvailableByCanchaAndDate(1L, fecha));

        verify(turnoRepository).findAll();
        verify(mapper, never()).convertValue(any(Turno.class), eq(TurnoResponseDTO.class));
    }
}