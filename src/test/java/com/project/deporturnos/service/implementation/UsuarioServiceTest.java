package com.project.deporturnos.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.domain.*;
import com.project.deporturnos.entity.dto.LockUnlockResponseDTO;
import com.project.deporturnos.entity.dto.UsuarioRequestUpdateDTO;
import com.project.deporturnos.entity.dto.UsuarioResponseDTO;
import com.project.deporturnos.entity.dto.UsuarioSimpleDTO;
import com.project.deporturnos.exception.BusinessRuleException;
import com.project.deporturnos.exception.InvalidEmailException;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.exception.UserAlreadyExistsException;
import com.project.deporturnos.repository.IUsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private UsuarioService usuarioService;

    /* Metodo update() */
    @Test
    void update_Success() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan Test");
        usuario.setEmail("juantest@gmail.com");
        usuario.setPassword("password123");

        UsuarioRequestUpdateDTO userRequestUpdateDTO = new UsuarioRequestUpdateDTO();
        userRequestUpdateDTO.setNombre("Juan Updated");
        userRequestUpdateDTO.setEmail("juanUpdated@email.com");
        userRequestUpdateDTO.setPassword("newPassword123");

        Usuario usuarioUpdated = new Usuario();
        usuarioUpdated.setId(1L);
        usuarioUpdated.setNombre("Juan Updated");
        usuarioUpdated.setEmail("juanUpdated@email.com");
        usuarioUpdated.setPassword("encodedPassword");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioUpdated);
        when(mapper.convertValue(any(Usuario.class), eq(UsuarioResponseDTO.class)))
                .thenReturn(new UsuarioResponseDTO(1L, "Juan Updated", "juanUpdated@email.com", "encodedPassword"));

        UsuarioResponseDTO userResponseDTO = usuarioService.update(1L, userRequestUpdateDTO);

        // Verificaciones
        assertEquals("Juan Updated", userResponseDTO.getNombre());
        assertEquals("juanUpdated@email.com", userResponseDTO.getEmail());

        // Verificar interacciones con mocks
        verify(usuarioRepository).findById(1L);
        verify(passwordEncoder).encode("newPassword123");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void update_UserNotFound() {

        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        UsuarioRequestUpdateDTO userRequestUpdateDTO = new UsuarioRequestUpdateDTO();
        userRequestUpdateDTO.setNombre("Juan Updated");
        userRequestUpdateDTO.setEmail("juanUpdated@email.com");

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.update(1L, userRequestUpdateDTO));

        verify(usuarioRepository).findById(1L);
    }

    @Test
    void update_InvalidEmail() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan Test");
        usuario.setEmail("juanTest@email.com");

        UsuarioRequestUpdateDTO userRequestUpdateDTO = new UsuarioRequestUpdateDTO();
        userRequestUpdateDTO.setEmail("invalid-email");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThrows(InvalidEmailException.class, () -> usuarioService.update(1L, userRequestUpdateDTO));

        verify(usuarioRepository).findById(1L);
    }

    @Test
    void update_UserAlreadyExists() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan Test");
        usuario.setEmail("juantest@gmail.com");

        UsuarioRequestUpdateDTO userRequestUpdateDTO = new UsuarioRequestUpdateDTO();
        userRequestUpdateDTO.setEmail("existinguser@email.com"); // Email ya usado por otro usuario

        Usuario existingUser = new Usuario();
        existingUser.setId(2L); // Un usuario diferente con el mismo email

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail("existinguser@email.com")).thenReturn(Optional.of(existingUser));

        assertThrows(UserAlreadyExistsException.class, () -> usuarioService.update(1L, userRequestUpdateDTO));

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).findByEmail("existinguser@email.com");
    }

    /* Metodo getAll() */
    @Test
    void getPaginatedData_Success() {
        Usuario usuario1 = new Usuario(1L, "Juan", "juanTest@email.com", "password123", "3512797689", Rol.CLIENTE, true);
        Usuario usuario2 = new Usuario(2L, "Sofia", "sofiaTest@email.com", "password123", "3512797686", Rol.ADMIN, true);

        List<Usuario> usuariosList = Arrays.asList(usuario1, usuario2);

        Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
        Page<Usuario> usuariosPage = new PageImpl<>(usuariosList, pageable, usuariosList.size());

        UsuarioSimpleDTO usuarioSimpleDTO1 = new UsuarioSimpleDTO(1L, "Juan", "juanTest@email.com", "3512797689", Rol.CLIENTE, true, false);
        UsuarioSimpleDTO usuarioSimpleDTO2 = new UsuarioSimpleDTO(2L, "Sofia", "sofiaTest@email.com", "3512797686", Rol.CLIENTE, true, false);


        when(usuarioRepository.findAllByDeletedFalse(pageable)).thenReturn(usuariosPage);

        when(mapper.convertValue(usuario1, UsuarioSimpleDTO.class)).thenReturn(usuarioSimpleDTO1);
        when(mapper.convertValue(usuario2, UsuarioSimpleDTO.class)).thenReturn(usuarioSimpleDTO2);


        Page<UsuarioSimpleDTO> resultPage = usuarioService.getPaginatedData(pageable);

        assertNotNull(resultPage);

        List<UsuarioSimpleDTO> resultList = resultPage.getContent();
        assertEquals(2, resultList.size());
        assertEquals("Juan", resultList.get(0).getNombre());
        assertEquals("Sofia", resultList.get(1).getNombre());

        verify(usuarioRepository).findAllByDeletedFalse(pageable);
        verify(mapper).convertValue(usuario1, UsuarioSimpleDTO.class);
        verify(mapper).convertValue(usuario2, UsuarioSimpleDTO.class);
    }

    @Test
void getPaginatedData_UserNotFound(){
    Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
    
    Page<Usuario> emptyPage = Page.empty(pageable); 

    when(usuarioRepository.findAllByDeletedFalse(pageable)).thenReturn(emptyPage);

    assertThrows(ResourceNotFoundException.class, () -> usuarioService.getPaginatedData(pageable));

    verify(usuarioRepository).findAllByDeletedFalse(pageable);
    
    verify(mapper, never()).convertValue(any(Usuario.class), eq(UsuarioSimpleDTO.class));
}

    /* Metodo delete() */
    @Test
    void delete_Success() {
        Turno turno = new Turno();
        Reserva reserva = new Reserva();
        reserva.setDeleted(false);
        reserva.setTurno(turno);
        turno.setReservas(Set.of(reserva));

        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setNombre("Juan");
        usuario.setEmail("juantest@email.com");
        usuario.setDeleted(false);
        usuario.setReservas(Set.of(reserva));

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        usuarioService.delete(2L);

        assertTrue(usuario.isDeleted());
        assertFalse(usuario.isActivada());
        assertTrue(reserva.isDeleted());
        assertEquals(TurnoState.DISPONIBLE, turno.getEstado());
        verify(usuarioRepository).findById(2L);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void delete_UserNotFound() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.delete(1L));

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    /* Metodo changeRole() */
    @Test
    void changeRole_Success() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setRol(Rol.CLIENTE);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(mapper.convertValue(usuario, UsuarioResponseDTO.class)).thenReturn(new UsuarioResponseDTO(2L,
                "Juan Updated", "juanUpdated@email.com", "encodedPassword", null, Rol.ADMIN, true, false));

        UsuarioResponseDTO usuarioResponseDTO = usuarioService.changeRole(2L);

        assertEquals(Rol.ADMIN, usuarioResponseDTO.getRol());
        verify(usuarioRepository).findById(2L);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void changeRole_UserNotFound() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.changeRole(3L));

        assertEquals("Usuario no encontrado.", exception.getMessage());
        verify(usuarioRepository).findById(3L);
    }

    @Test
    void changeRole_AdminSupreme() {
        Usuario adminSupremo = new Usuario();
        adminSupremo.setId(1L); // El admin supremo tiene id=1

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminSupremo));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> usuarioService.changeRole(1L));

        assertEquals("No es posible cambiar el rol al administrador supremo.", exception.getMessage());
        verify(usuarioRepository).findById(1L);
    }

    /* Metodo lockUnlock() */
    @Test
    void lockUnlock_Success() {
        // Usuario de prueba:
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setNombre("Juan Test");
        usuario.setEmail("juanTest@email.com");
        usuario.setActivada(Boolean.TRUE);

        // Simulamos que se encuentra el usuario en el repo
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        // Usuario de prueba luego de guardado:
        Usuario usuarioSaved = new Usuario();
        usuarioSaved.setId(2L);
        usuarioSaved.setNombre("Juan Test");
        usuarioSaved.setEmail("juanTest@email.com");
        usuarioSaved.setActivada(Boolean.FALSE); // Luego de ejecutar el metodo lock/unlock

        // Simulamos que se guarda el usuario en el repo
        when(usuarioRepository.save(usuario)).thenReturn(usuarioSaved);

        // Simulamos el mapper
        when(mapper.convertValue(any(Usuario.class), eq(LockUnlockResponseDTO.class)))
                .thenReturn(new LockUnlockResponseDTO(2L, "juanTest@email.com", false));

        // Ejecutamos el metodo
        LockUnlockResponseDTO lockUnlockResponseDTO = usuarioService.lockUnlock(2L);

        // Verificaciones
        assertNotNull(lockUnlockResponseDTO);
        assertEquals(2L, lockUnlockResponseDTO.getId());
        assertEquals("juanTest@email.com", lockUnlockResponseDTO.getEmail());

        // Verificamos interacciones con los mocks
        verify(usuarioRepository).findById(2L);
        verify(usuarioRepository).save(any(Usuario.class));
        verify(mapper).convertValue(any(Usuario.class), eq(LockUnlockResponseDTO.class));
    }

    @Test
    void lockUnlock_UserNotFound() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.lockUnlock(3L));

        verify(usuarioRepository).findById(3L);
    }

    @Test
    void lockUnlock_AdminSupreme() {
        Usuario adminSupremo = new Usuario();
        adminSupremo.setId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminSupremo));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> usuarioService.lockUnlock(1L));

        assertEquals("No es posible bloquear al administrador supremo.", exception.getMessage());

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

}