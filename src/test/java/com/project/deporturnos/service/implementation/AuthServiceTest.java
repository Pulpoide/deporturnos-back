package com.project.deporturnos.service.implementation;

import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.LoginRequestDTO;
import com.project.deporturnos.entity.dto.RegistrationRequestDTO;
import com.project.deporturnos.entity.dto.RegistrationResponseDTO;
import com.project.deporturnos.entity.dto.VerifyUserDTO;
import com.project.deporturnos.repository.IUsuarioRepository;
import com.project.deporturnos.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private IUsuarioRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoderMock;

    @Mock
    private EmailService emailServiceMock;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;


    /* Registro de un Usuario */
    @Test
    void signup() {
        RegistrationRequestDTO requestDTO = new RegistrationRequestDTO();
        requestDTO.setEmail("email@email.com");
        requestDTO.setPassword("password123");
        requestDTO.setNombre("Juan Test");
        requestDTO.setTelefono("3512164399");
        RegistrationResponseDTO registrationResponseDTO = authService.signup(requestDTO);

        assertEquals(registrationResponseDTO.getEmail(), "email@email.com");
        assertEquals(registrationResponseDTO.getNombre(), "Juan Test");
        assertEquals(registrationResponseDTO.getTelefono(), "3512164399");
    }

    /* Login de un Usuario, metodo authenticate() */
    @Test
    void login_Success() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail("email@email.com");
        loginRequestDTO.setPassword("password123");

        Usuario mockUsuario = new Usuario();
        mockUsuario.setEmail("email@email.com");
        mockUsuario.setPassword("password123");
        mockUsuario.setActivada(true);
        mockUsuario.setDeleted(false);

        when(userRepository.findByEmail(loginRequestDTO.getEmail())).thenReturn(Optional.of(mockUsuario));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenAnswer(invocation -> null);

        Usuario authenticatedUser = authService.authenticate(loginRequestDTO);

        // Verifica que el email y el password sean correctos
        assertNotNull(authenticatedUser);
        assertEquals(loginRequestDTO.getEmail(), authenticatedUser.getEmail());
        assertEquals(loginRequestDTO.getPassword(), authenticatedUser.getPassword());

        // Verifica que los mocks fueron llamados correctamente
        verify(userRepository).findByEmail(loginRequestDTO.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

    }

    @Test
    void authenticate_UserNotFound() {
        // Dado un email que no existe
        LoginRequestDTO loginRequest = new LoginRequestDTO("nonexistent@example.com", "password123");

        // Cuando el repositorio no encuentra al usuario, retorna un Optional vacío
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Entonces esperamos una excepción
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticate(loginRequest);
        });

        // Verificamos que el mensaje sea correcto
        assertEquals("Usuario no encontrado.", exception.getMessage());
    }

    @Test
    void authenticate_AccountNotVerified() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("verifieduser@example.com", "password123");

        // Creamos un usuario simulado no verificado
        Usuario user = new Usuario();
        user.setEmail(loginRequest.getEmail());
        user.setPassword("hashedpassword");
        user.setActivada(false); // No está verificado
        user.setDeleted(false);

        // Simulamos que el usuario es encontrado por el repositorio
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.authenticate(loginRequest);
        });

        // Verificamos que el repositorio fue llamado con el email correcto
        verify(userRepository).findByEmail(loginRequest.getEmail());

        // Verificamos que el mensaje de excepción sea correcto
        assertEquals("Cuenta no verificada. Por favor verifique su cuenta.", exception.getMessage());
    }

    /* Metodo verifyUser() */
    @Test
    void verifyUser_UserNotFound() {
        VerifyUserDTO verifyUserDTO = new VerifyUserDTO();
        verifyUserDTO.setEmail("email@email.com");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.verifyUser(verifyUserDTO);
        });

        assertEquals("Usuario no encontrado.", exception.getMessage());
    }

    @Test
    void verifyUser_CodeExpired() {
        VerifyUserDTO verifyUserDTO = new VerifyUserDTO();
        verifyUserDTO.setEmail("email@email.com");
        verifyUserDTO.setVerificationCode("123456");

        Usuario user = new Usuario();
        user.setEmail("email@email.com");
        user.setPassword("hashedpassword");
        user.setActivada(false);
        user.setDeleted(false);
        user.setVerificationCodeExpiresAt(LocalDateTime.of(2024, 9, 25, 0, 0));
        user.setVerificationCode("123456");

        when(userRepository.findByEmail(verifyUserDTO.getEmail())).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.verifyUser(verifyUserDTO);
        });

        assertEquals("Código de verificación expirado.", exception.getMessage());
    }

    @Test
    void verifyUser_InvalidCode() {
        VerifyUserDTO verifyUserDTO = new VerifyUserDTO();
        verifyUserDTO.setEmail("email@email.com");
        verifyUserDTO.setVerificationCode("invalidcode");

        Usuario user = new Usuario();
        user.setEmail("email@email.com");
        user.setPassword("hashedpassword");
        user.setActivada(false);
        user.setDeleted(false);
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10)); // Aún no ha expirado

        when(userRepository.findByEmail(verifyUserDTO.getEmail())).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.verifyUser(verifyUserDTO);
        });

        assertEquals("Código de verificación no válido.", exception.getMessage());
    }

    /* Metodo resendVerificationCode() */
    @Test
    void resendVerificationCode_UserNotFound() {
        String email = "email@email.com";

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.resendVerificationCode(email);
        });

        assertEquals("Usuario no encontrado.", exception.getMessage());
    }

    @Test
    void resendVerificationCode_AccountAlreadyVerified() {
        String email = "email@email.com";

        Usuario user = new Usuario();
        user.setEmail(email);
        user.setPassword("hashedpassword");
        user.setActivada(true);
        user.setDeleted(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.resendVerificationCode(email);
        });

        assertEquals("La cuenta ya está verificada.", exception.getMessage());
    }

}