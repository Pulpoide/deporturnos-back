package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.*;
import com.project.deporturnos.entity.dto.ReservaRequestDTO;
import com.project.deporturnos.entity.dto.ReservaRequestUpdateDTO;
import com.project.deporturnos.entity.dto.ReservaResponseDTO;
import com.project.deporturnos.entity.dto.UsuarioResponseDTO;
import com.project.deporturnos.exception.ReservaAlreadyCancelledException;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.exception.TurnoAlreadyReservedException;
import com.project.deporturnos.repository.IReservaRepository;
import com.project.deporturnos.repository.ITurnoRepository;
import com.project.deporturnos.repository.IUsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @InjectMocks
    private ReservaService reservaService;

    @Mock
    private IReservaRepository reservaRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private ITurnoRepository turnoRepository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private NotificationService notificationService;

    /* Metodo save() */
    @Test
    void save_Success() {
        ReservaRequestDTO reservaRequestDTO = new ReservaRequestDTO();
        reservaRequestDTO.setUsuarioId(1L);
        reservaRequestDTO.setTurnoId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Turno turno = new Turno();
        turno.setId(1L);
        turno.setEstado(TurnoState.DISPONIBLE);

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setUsuario(usuario);
        reserva.setTurno(turno);
        reserva.setEstado(ReservaState.CONFIRMADA);
        reserva.setFecha(LocalDate.now());

        ReservaResponseDTO reservaResponseDTO = new ReservaResponseDTO();
        reservaResponseDTO.setId(1L);
        reservaResponseDTO.setFecha(LocalDate.now());
        reservaResponseDTO.setEstado(ReservaState.CONFIRMADA);
        reservaResponseDTO.setUsuario(new UsuarioResponseDTO(1L, "John Test", "john@email.com", "password123"));
        reservaResponseDTO.setTurno(turno);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);
        when(mapper.convertValue(any(Reserva.class), eq(ReservaResponseDTO.class))).thenReturn(reservaResponseDTO);
        when(mapper.convertValue(any(ReservaRequestDTO.class), eq(Reserva.class))).thenReturn(reserva);

        ReservaResponseDTO result = reservaService.save(reservaRequestDTO);

        assertNotNull(result);
        assertEquals(result.getId(), reservaResponseDTO.getId());
        assertEquals(result.getEstado(), reservaResponseDTO.getEstado());

        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void save_UserNotFound() {
        ReservaRequestDTO reservaRequestDTO = new ReservaRequestDTO();
        reservaRequestDTO.setUsuarioId(1L);
        reservaRequestDTO.setTurnoId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservaService.save(reservaRequestDTO));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void save_TurnoNotFound() {
        ReservaRequestDTO reservaRequestDTO = new ReservaRequestDTO();
        reservaRequestDTO.setUsuarioId(1L);
        reservaRequestDTO.setTurnoId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(turnoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservaService.save(reservaRequestDTO));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void save_TurnoAlreadyReserved() {
        ReservaRequestDTO reservaRequestDTO = new ReservaRequestDTO();
        reservaRequestDTO.setUsuarioId(1L);
        reservaRequestDTO.setTurnoId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Turno turno = new Turno();
        turno.setId(1L);
        turno.setEstado(TurnoState.RESERVADO);

        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setFecha(LocalDate.now());

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turno));
        when(mapper.convertValue(any(ReservaRequestDTO.class), eq(Reserva.class))).thenReturn(reserva);

        assertThrows(TurnoAlreadyReservedException.class, () -> reservaService.save(reservaRequestDTO));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    /* Metodo getAll() */
    @Test
    void getAll_Success() {
        Reserva reserva1 = new Reserva();
        reserva1.setId(1L);
        reserva1.setFecha(LocalDate.now());
        reserva1.setEstado(ReservaState.CONFIRMADA);

        Reserva reserva2 = new Reserva();
        reserva2.setId(2L);
        reserva2.setFecha(LocalDate.now());
        reserva2.setEstado(ReservaState.CONFIRMADA);

        List<Reserva> reservas = Arrays.asList(reserva1, reserva2);

        ReservaResponseDTO reservaResponseDTO1 = new ReservaResponseDTO();
        reservaResponseDTO1.setId(1L);
        reservaResponseDTO1.setFecha(LocalDate.now());
        reservaResponseDTO1.setEstado(ReservaState.CONFIRMADA);

        ReservaResponseDTO reservaResponseDTO2 = new ReservaResponseDTO();
        reservaResponseDTO2.setId(2L);
        reservaResponseDTO2.setFecha(LocalDate.now());
        reservaResponseDTO2.setEstado(ReservaState.CONFIRMADA);

        when(reservaRepository.findAllByDeletedFalse()).thenReturn(reservas);
        when(mapper.convertValue(reserva1, ReservaResponseDTO.class)).thenReturn(reservaResponseDTO1);
        when(mapper.convertValue(reserva2, ReservaResponseDTO.class)).thenReturn(reservaResponseDTO2);

        List<ReservaResponseDTO> result = reservaService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(reservaRepository).findAllByDeletedFalse();
        verify(mapper).convertValue(reserva1, ReservaResponseDTO.class);
        verify(mapper).convertValue(reserva2, ReservaResponseDTO.class);
    }

    @Test
    void getAll_NotFound() {
        when(reservaRepository.findAllByDeletedFalse()).thenReturn(Collections.emptyList());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> reservaService.getAll());

        assertEquals("No se encontraron reservas para listar.", exception.getMessage());

        verify(reservaRepository).findAllByDeletedFalse();
        verify(mapper, never()).convertValue(any(Reserva.class), eq(ReservaResponseDTO.class));
    }

    /* Metodo update() */
    @Test
    public void update_Success() {
        Long reservaId = 7L;
        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setFecha(LocalDate.of(2024, 10, 1));
        reserva.setEstado(ReservaState.CANCELADA);

        Turno turno = new Turno();
        turno.setEstado(TurnoState.DISPONIBLE);
        reserva.setTurno(turno);

        Usuario usuario = new Usuario();
        usuario.setId(5L);
        reserva.setUsuario(usuario);

        ReservaRequestUpdateDTO request = new ReservaRequestUpdateDTO();
        request.setEstado(ReservaState.CONFIRMADA);

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(Mockito.any(Reserva.class))).thenReturn(reserva);
        when(mapper.convertValue(reserva, ReservaResponseDTO.class)).thenReturn(new ReservaResponseDTO());

        ReservaResponseDTO response = reservaService.update(reservaId, request);

        assertNotNull(response);
        assertEquals(ReservaState.CONFIRMADA, reserva.getEstado());
        assertEquals(TurnoState.RESERVADO, turno.getEstado());

        verify(reservaRepository).save(reserva);
    }

    @Test
    public void update_NotFound() {
        Long reservaId = 7L;
        ReservaRequestUpdateDTO request = new ReservaRequestUpdateDTO();
        request.setEstado(ReservaState.CONFIRMADA);

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservaService.update(reservaId, request));

        verify(reservaRepository, never()).save(Mockito.any(Reserva.class));
    }

    @Test
    public void update_TurnoAlreadyReserved() {
        Long reservaId = 7L;
        Long turnoId = 3L;

        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado(ReservaState.CANCELADA);
        reserva.setFecha(LocalDate.now());

        Turno turno = new Turno();
        turno.setId(turnoId);
        turno.setEstado(TurnoState.RESERVADO);

        reserva.setTurno(new Turno());

        ReservaRequestUpdateDTO request = new ReservaRequestUpdateDTO();
        request.setTurnoId(turnoId);

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(turnoRepository.findById(turnoId)).thenReturn(Optional.of(turno));

        assertThrows(TurnoAlreadyReservedException.class, () -> reservaService.update(reservaId, request));

        verify(reservaRepository, never()).save(Mockito.any(Reserva.class));
    }

    @Test
    public void update_UserNotFound() {
        Long reservaId = 7L;
        Long usuarioId = 5L;

        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado(ReservaState.CANCELADA);
        reserva.setTurno(new Turno());
        reserva.setFecha(LocalDate.now());

        ReservaRequestUpdateDTO request = new ReservaRequestUpdateDTO();
        request.setUsuarioId(usuarioId);

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservaService.update(reservaId, request));

        verify(reservaRepository, never()).save(Mockito.any(Reserva.class));
    }

    @Test
    public void update_TurnoNotFound() {
        Long reservaId = 7L;
        Long turnoId = 3L;

        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado(ReservaState.CANCELADA);
        reserva.setTurno(new Turno());
        reserva.setFecha(LocalDate.now());

        ReservaRequestUpdateDTO request = new ReservaRequestUpdateDTO();
        request.setTurnoId(turnoId);

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(turnoRepository.findById(turnoId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservaService.update(reservaId, request));

        verify(reservaRepository, never()).save(Mockito.any(Reserva.class));
    }

    /* Metodo delete() */
    @Test
    public void delete_Success() {
        Long reservaId = 1L;

        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        Turno turno = new Turno();
        turno.setEstado(TurnoState.RESERVADO);
        reserva.setTurno(turno);

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));

        reservaService.delete(reservaId);

        assertTrue(reserva.isDeleted());
        assertEquals(TurnoState.DISPONIBLE, turno.getEstado());

        verify(reservaRepository).save(reserva);
    }

    @Test
    public void delete_NotFound() {
        Long reservaId = 1L;

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservaService.delete(reservaId));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    /* Metodo saveReservaByUser() */
    @Test
    public void saveReservaByUser_Success() {
        ReservaRequestDTO request = new ReservaRequestDTO();
        request.setTurnoId(1L);

        Turno turno = new Turno();
        turno.setId(1L);
        turno.setEstado(TurnoState.DISPONIBLE);

        Usuario currentUser = new Usuario();
        currentUser.setId(1L);

        when(turnoRepository.findById(request.getTurnoId())).thenReturn(Optional.of(turno));
        when(mapper.convertValue(request, Reserva.class)).thenReturn(new Reserva());
        when(reservaRepository.save(any(Reserva.class))).thenReturn(new Reserva());
        when(mapper.convertValue(any(Reserva.class), eq(ReservaResponseDTO.class))).thenReturn(new ReservaResponseDTO());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(mock(Authentication.class));
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);

        ReservaResponseDTO response = reservaService.saveReservaByUser(request);

        assertNotNull(response);
        assertEquals(TurnoState.RESERVADO, turno.getEstado());
        verify(turnoRepository).findById(request.getTurnoId());
        verify(notificationService).sendNotificationReservationConfirmed(currentUser);
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    public void saveReservaByUser_TurnoAlreadyReserved() {
        ReservaRequestDTO request = new ReservaRequestDTO();
        request.setTurnoId(1L);

        Turno turno = new Turno();
        turno.setId(1L);
        turno.setEstado(TurnoState.RESERVADO);

        Usuario currentUser = new Usuario();
        currentUser.setId(2L);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);

        when(turnoRepository.findById(request.getTurnoId())).thenReturn(Optional.of(turno));

        assertThrows(TurnoAlreadyReservedException.class, () -> reservaService.saveReservaByUser(request));

        verify(turnoRepository).findById(request.getTurnoId());
        verify(reservaRepository, never()).save(any(Reserva.class));
        verify(notificationService, never()).sendNotificationReservationConfirmed(any(Usuario.class));
    }

    @Test
    public void saveReservaByUser_TurnoNotFound() {
        ReservaRequestDTO request = new ReservaRequestDTO();
        request.setTurnoId(1L);

        Usuario currentUser = new Usuario();
        currentUser.setId(2L);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);

        when(turnoRepository.findById(request.getTurnoId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservaService.saveReservaByUser(request));

        verify(turnoRepository).findById(request.getTurnoId());
        verify(reservaRepository, never()).save(any(Reserva.class));
        verify(notificationService, never()).sendNotificationReservationConfirmed(any(Usuario.class));
    }

    /* Metodo cancel() */
    @Test
    void cancel_Success() {
        Long reservaId = 1L;

        Usuario currentUser = new Usuario();
        currentUser.setId(2L);
        currentUser.setRol(Rol.CLIENTE);

        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado(ReservaState.CONFIRMADA);
        reserva.setUsuario(currentUser);

        Turno turno = new Turno();
        turno.setEstado(TurnoState.RESERVADO);
        reserva.setTurno(turno);

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        reservaService.cancel(reservaId);

        assertEquals(ReservaState.CANCELADA, reserva.getEstado());
        assertEquals(TurnoState.DISPONIBLE, turno.getEstado());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void cancel_ReservaAlreadyCancelled() {
        Long reservaId = 1L;
        Usuario currentUser = new Usuario();
        currentUser.setId(2L);
        currentUser.setRol(Rol.CLIENTE);

        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado(ReservaState.CANCELADA);
        reserva.setUsuario(currentUser);

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, null));

        assertThrows(ReservaAlreadyCancelledException.class, () -> reservaService.cancel(reservaId));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void cancel_InsufficientAuthentication() {
        Long reservaId = 1L;

        Usuario currentUser = new Usuario();
        currentUser.setId(2L);
        currentUser.setRol(Rol.CLIENTE);

        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(3L);

        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado(ReservaState.CONFIRMADA);
        reserva.setUsuario(otroUsuario);

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(InsufficientAuthenticationException.class, () -> reservaService.cancel(reservaId));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void cancel_NotFound() {
        Long reservaId = 1L;
        Usuario currentUser = new Usuario();
        currentUser.setId(1L);
        currentUser.setRol(Rol.CLIENTE);

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(currentUser, null));

        assertThrows(ResourceNotFoundException.class, () -> reservaService.cancel(reservaId));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

}