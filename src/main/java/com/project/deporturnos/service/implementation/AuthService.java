package com.project.deporturnos.service.implementation;

import com.project.deporturnos.entity.domain.Rol;
import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.LoginRequestDTO;
import com.project.deporturnos.entity.dto.LoginResponseDTO;
import com.project.deporturnos.entity.dto.RegistrationRequestDTO;
import com.project.deporturnos.entity.dto.RegistrationResponseDTO;
import com.project.deporturnos.repository.IUsuarioRepository;
import com.project.deporturnos.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUsuarioRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public LoginResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        Usuario user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtService.getToken(user);

        return LoginResponseDTO.builder()
                .token(token)
                .build();
    }

    public RegistrationResponseDTO register(RegistrationRequestDTO request) {
        Usuario user = Usuario.builder()
                .nombre(request.getNombre())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .rol(Rol.CLIENTE)
                .cuentaActivada(true)
                .build();

        userRepository.save(user);

        return RegistrationResponseDTO.builder()
                .id(user.getId())
                .nombre(user.getNombre())
                .email(user.getEmail())
                .telefono(user.getTelefono())
                .build();
    }

}
