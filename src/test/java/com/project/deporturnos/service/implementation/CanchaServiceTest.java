package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.*;
import com.project.deporturnos.entity.dto.CanchaRequestDTO;
import com.project.deporturnos.entity.dto.CanchaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.CanchaResponseDTO;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.repository.ICanchaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CanchaServiceTest {

    @InjectMocks
    private CanchaService canchaService;

    @Mock
    private ICanchaRepository canchaRepository;

    @Mock
    private ObjectMapper mapper;


    /* Metodo save() */
    @Test
    void save_Success() {
        CanchaRequestDTO canchaRequestDTO = new CanchaRequestDTO("Cancha 1", "Fútbol 11", 1500, true, "Descripción", Deporte.FUTBOL);

        Cancha cancha = new Cancha();
        cancha.setNombre("Cancha 1");
        cancha.setTipo("Fútbol 11");
        cancha.setPrecioHora(1500);
        cancha.setDisponibilidad(true);

        CanchaResponseDTO expectedResponse = new CanchaResponseDTO();
        expectedResponse.setNombre("Cancha 1");
        expectedResponse.setTipo("Fútbol 11");

        when(mapper.convertValue(canchaRequestDTO, Cancha.class)).thenReturn(cancha);
        when(canchaRepository.save(cancha)).thenReturn(cancha);
        when(mapper.convertValue(cancha, CanchaResponseDTO.class)).thenReturn(expectedResponse);

        CanchaResponseDTO actualResponse = canchaService.save(canchaRequestDTO);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getNombre(), actualResponse.getNombre());
        assertEquals(expectedResponse.getTipo(), actualResponse.getTipo());

        verify(canchaRepository).save(cancha);
        verify(mapper).convertValue(cancha, CanchaResponseDTO.class);
        verify(mapper).convertValue(canchaRequestDTO, Cancha.class);
    }

    /* Metodo getAll() */
    @Test
    void getAll_Success() {
        List<Cancha> canchas = new ArrayList<>();

        Cancha cancha1 = new Cancha();
        cancha1.setNombre("Cancha 1");
        cancha1.setTipo("Fútbol 11");

        Cancha cancha2 = new Cancha();
        cancha2.setNombre("Cancha 2");
        cancha2.setTipo("Fútbol 5");

        canchas.add(cancha1);
        canchas.add(cancha2);

        List<CanchaResponseDTO> expectedResponse = new ArrayList<>();

        CanchaResponseDTO canchaResponseDTO1 = new CanchaResponseDTO();
        canchaResponseDTO1.setNombre("Cancha 1");
        canchaResponseDTO1.setTipo("Fútbol 11");

        CanchaResponseDTO canchaResponseDTO2 = new CanchaResponseDTO();
        canchaResponseDTO2.setNombre("Cancha 2");
        canchaResponseDTO2.setTipo("Fútbol 5");

        expectedResponse.add(canchaResponseDTO1);
        expectedResponse.add(canchaResponseDTO2);

        when(canchaRepository.findAllByDeletedFalse()).thenReturn(canchas);
        when(mapper.convertValue(cancha1, CanchaResponseDTO.class)).thenReturn(canchaResponseDTO1);
        when(mapper.convertValue(cancha2, CanchaResponseDTO.class)).thenReturn(canchaResponseDTO2);

        List<CanchaResponseDTO> actualResponse = canchaService.getAll();

        assertNotNull(actualResponse);
        assertEquals(2, actualResponse.size());
        assertEquals(expectedResponse.get(0).getNombre(), actualResponse.get(0).getNombre());
        assertEquals(expectedResponse.get(1).getTipo(), actualResponse.get(1).getTipo());

        verify(canchaRepository).findAllByDeletedFalse();
        verify(mapper).convertValue(cancha1, CanchaResponseDTO.class);
        verify(mapper).convertValue(cancha2, CanchaResponseDTO.class);
    }

    @Test
    void getAll_NotFound() {
        when(canchaRepository.findAllByDeletedFalse()).thenReturn(Collections.emptyList());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> canchaService.getAll());
        assertEquals("No se encontraron canchas para listar.", exception.getMessage());

        verify(canchaRepository).findAllByDeletedFalse();
        verifyNoInteractions(mapper);
    }

    @Test
    void update_Success() {
        Long canchaId = 1L;

        CanchaRequestUpdateDTO canchaRequestUpdateDTO = new CanchaRequestUpdateDTO();
        canchaRequestUpdateDTO.setNombre("Cancha Actualizada");
        canchaRequestUpdateDTO.setTipo("Fútbol 11");
        canchaRequestUpdateDTO.setPrecioHora(2000);
        canchaRequestUpdateDTO.setDisponibilidad(true);
        canchaRequestUpdateDTO.setDeporte(Deporte.FUTBOL);
        canchaRequestUpdateDTO.setDescripcion("Descripción Actualizada");

        Cancha existingCancha = new Cancha();
        existingCancha.setId(canchaId);
        existingCancha.setNombre("Cancha Antigua");
        existingCancha.setTipo("Fútbol 5");
        existingCancha.setPrecioHora(1500);
        existingCancha.setDisponibilidad(false);
        existingCancha.setDeporte(Deporte.FUTBOL);
        existingCancha.setDescripcion("Descripción antigua");

        Cancha updatedCancha = new Cancha();
        updatedCancha.setId(canchaId);
        updatedCancha.setNombre("Cancha Actualizada");
        updatedCancha.setTipo("Fútbol 11");
        updatedCancha.setPrecioHora(2000);
        updatedCancha.setDisponibilidad(true);
        updatedCancha.setDeporte(Deporte.FUTBOL);
        updatedCancha.setDescripcion("Descripción Actualizada");

        CanchaResponseDTO expectedResponse = new CanchaResponseDTO();
        expectedResponse.setNombre("Cancha Actualizada");
        expectedResponse.setTipo("Fútbol 11");
        expectedResponse.setPrecioHora(2000);
        expectedResponse.setDisponibilidad(true);
        expectedResponse.setDeporte(Deporte.FUTBOL);
        expectedResponse.setDescripcion("Descripción Actualizada");

        when(canchaRepository.findById(canchaId)).thenReturn(Optional.of(existingCancha));
        when(canchaRepository.save(existingCancha)).thenReturn(updatedCancha);
        when(mapper.convertValue(updatedCancha, CanchaResponseDTO.class)).thenReturn(expectedResponse);

        CanchaResponseDTO actualResponse = canchaService.update(canchaId, canchaRequestUpdateDTO);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getNombre(), actualResponse.getNombre());
        assertEquals(expectedResponse.getTipo(), actualResponse.getTipo());
        assertEquals(expectedResponse.getPrecioHora(), actualResponse.getPrecioHora());
        assertEquals(expectedResponse.getDescripcion(), actualResponse.getDescripcion());
        assertTrue(expectedResponse.isDisponibilidad());

        verify(canchaRepository).findById(canchaId);
        verify(canchaRepository).save(existingCancha);
        verify(mapper).convertValue(updatedCancha, CanchaResponseDTO.class);
    }

    @Test
    void update_NotFound() {
        Long canchaId = 50L;
        CanchaRequestUpdateDTO canchaRequestUpdateDTO = new CanchaRequestUpdateDTO();

        when(canchaRepository.findById(canchaId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> canchaService.update(canchaId, canchaRequestUpdateDTO));

        assertEquals("Cancha no encontrada.", exception.getMessage());

        verify(canchaRepository).findById(canchaId);
        verifyNoMoreInteractions(canchaRepository);
        verifyNoInteractions(mapper);
    }

    /* Metodo delete() */
    @Test
    void delete_Success() {
        Long canchaId = 1L;

        Reserva reserva = new Reserva();
        reserva.setDeleted(false);

        Turno turno = new Turno();
        turno.setDeleted(false);
        turno.setEstado(TurnoState.DISPONIBLE);
        turno.setReservas(new HashSet<>(Set.of(reserva))); // Usamos un HashSet para la relación OneToMany

        Cancha cancha = new Cancha();
        cancha.setId(canchaId);
        cancha.setDeleted(false);
        cancha.setTurnos(new HashSet<>(Set.of(turno)));

        when(canchaRepository.findById(canchaId)).thenReturn(Optional.of(cancha));

        canchaService.delete(canchaId);

        assertTrue(cancha.isDeleted());
        assertTrue(turno.isDeleted());
        assertEquals(TurnoState.BORRADO, turno.getEstado());
        assertTrue(reserva.isDeleted());

        verify(canchaRepository).findById(canchaId);
        verify(canchaRepository).save(cancha);
    }

    @Test
    void delete_NotFound() {
        Long canchaId = 66L;

        when(canchaRepository.findById(canchaId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> canchaService.delete(canchaId));

        assertEquals("Cancha no encontrada.", exception.getMessage());

        verify(canchaRepository).findById(canchaId);
        verifyNoMoreInteractions(canchaRepository);
    }

    /* Metodo getAvailableByDeporte() */
    @Test
    void getAvailableByDeporte_Success() {
        String deporte = "FUTBOL";

        Cancha cancha1 = new Cancha();
        cancha1.setId(1L);
        cancha1.setDisponibilidad(true);
        cancha1.setDeleted(false);
        cancha1.setDeporte(Deporte.FUTBOL); // Suponiendo que Deporte es un Enum

        Cancha cancha2 = new Cancha();
        cancha2.setId(2L);
        cancha2.setDisponibilidad(true);
        cancha2.setDeleted(false);
        cancha2.setDeporte(Deporte.FUTBOL);

        CanchaResponseDTO canchaResponseDTO1 = new CanchaResponseDTO();
        CanchaResponseDTO canchaResponseDTO2 = new CanchaResponseDTO();

        when(canchaRepository.findAll()).thenReturn(List.of(cancha1, cancha2));
        when(mapper.convertValue(cancha1, CanchaResponseDTO.class)).thenReturn(canchaResponseDTO1);
        when(mapper.convertValue(cancha2, CanchaResponseDTO.class)).thenReturn(canchaResponseDTO2);

        List<CanchaResponseDTO> result = canchaService.getAvailableByDeporte(deporte);

        assertEquals(2, result.size());
        verify(canchaRepository).findAll();
        verify(mapper).convertValue(cancha1, CanchaResponseDTO.class);
        verify(mapper).convertValue(cancha2, CanchaResponseDTO.class);
    }

    @Test
    void getAvailableByDeporte_NotFound() {
        when(canchaRepository.findAll()).thenReturn(Collections.emptyList());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> canchaService.getAvailableByDeporte("FUTBOL"));

        assertEquals("No se encontraron canchas para listar.", exception.getMessage());

        verify(canchaRepository).findAll();
    }
}