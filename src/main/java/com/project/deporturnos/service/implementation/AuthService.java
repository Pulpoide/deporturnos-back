package com.project.deporturnos.service.implementation;

import com.project.deporturnos.entity.domain.Rol;
import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.LoginRequestDTO;
import com.project.deporturnos.entity.dto.RegistrationRequestDTO;
import com.project.deporturnos.entity.dto.RegistrationResponseDTO;
import com.project.deporturnos.entity.dto.VerifyUserDTO;
import com.project.deporturnos.exception.InvalidEmailException;
import com.project.deporturnos.exception.InvalidPasswordException;
import com.project.deporturnos.exception.UserAlreadyExistsException;
import com.project.deporturnos.exception.VerificationEmailException;
import com.project.deporturnos.repository.IUsuarioRepository;
import com.project.deporturnos.security.JwtService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final IUsuarioRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public RegistrationResponseDTO signup(RegistrationRequestDTO request) {
        final Pattern emailPattern = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,}$");
        if (!emailPattern.matcher(request.getEmail()).matches()) {
            throw new InvalidEmailException("Correo electrónico no válido.");
        }

        final Pattern passPattern = Pattern.compile("^(?=\\w*\\d)(?=\\w*[a-z])\\S{8,16}$");
        if (!passPattern.matcher(request.getPassword()).matches()) {
            throw new InvalidPasswordException("Contraseña no válida.");
        }

        try {
            Usuario user = Usuario.builder()
                    .nombre(request.getNombre())
                    .email(request.getEmail())
                    .telefono(request.getTelefono())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .rol(Rol.CLIENTE)
                    .activada(false)
                    .verificationCode(generateVerificationCode())
                    .verificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();

            userRepository.save(user);

            sendVerificationEmailAsync(user);

            String token = jwtService.getToken(user);

            return RegistrationResponseDTO.builder()
                    .id(user.getId())
                    .nombre(user.getNombre())
                    .email(user.getEmail())
                    .token(token)
                    .telefono(user.getTelefono())
                    .build();

        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("El usuario ya existe.");
        }
    }

    @Async
    public void sendVerificationEmailAsync(Usuario user) {
        try {
            sendVerificationEmail(user);
        } catch (Exception e) {
            log.error("No se pudo enviar el email de verificación a {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private void sendVerificationEmail(Usuario user) {
        String subject = "Verificación de cuenta";
        String verificationCode = user.getVerificationCode();
        String body = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">¡Bienvenido a DeporTurnos!</h2>"
                + "<p style=\"font-size: 16px;\">Ingrese el siguiente código de verificación:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; "
                + "box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Código:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">"
                + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (MessagingException e) {
            throw new VerificationEmailException("Error al enviar el código de verificación.");
        }
    }

    public void verifyUser(VerifyUserDTO input) {
        Optional<Usuario> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()) {
            Usuario user = optionalUser.get();
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Código de verificación expirado.");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setActivada(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Código de verificación no válido.");
            }
        } else {
            throw new RuntimeException("Usuario no encontrado.");
        }
    }

    public void resendVerificationCode(String email) {
        Optional<Usuario> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            Usuario user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("La cuenta ya está verificada.");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Usuario no encontrado.");
        }
    }

    public Usuario authenticate(LoginRequestDTO input) {
        Usuario user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (!user.isActivada()) {
            throw new RuntimeException("Cuenta no verificada. Por favor verifique su cuenta.");
        }
        if (user.isDeleted()) {
            throw new RuntimeException("Usuario no encontrado.");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()));

        return user;
    }
}
