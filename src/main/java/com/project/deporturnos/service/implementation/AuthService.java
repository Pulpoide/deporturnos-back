package com.project.deporturnos.service.implementation;

import com.project.deporturnos.entity.domain.Rol;
import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.LoginRequestDTO;
import com.project.deporturnos.entity.dto.LoginResponseDTO;
import com.project.deporturnos.entity.dto.RegistrationRequestDTO;
import com.project.deporturnos.entity.dto.RegistrationResponseDTO;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.exception.UserAlreadyExistsException;
import com.project.deporturnos.repository.IUsuarioRepository;
import com.project.deporturnos.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUsuarioRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));
        Optional<Usuario> user = userRepository.findByEmail(loginRequestDTO.getEmail());

        if(user.isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }

        String token = jwtService.getToken(user.get());

        return LoginResponseDTO.builder()
                .token(token)
                .build();
    }

    public RegistrationResponseDTO register(RegistrationRequestDTO request) {
        Optional<Usuario> usuarioOptional = userRepository.findByEmail(request.getEmail());
       if (usuarioOptional.isPresent()) {
           throw new UserAlreadyExistsException("El usuario ya existe.");
       }


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
