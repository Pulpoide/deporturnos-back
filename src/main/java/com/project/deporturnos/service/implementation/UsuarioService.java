package com.project.deporturnos.service.implementation;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.deporturnos.entity.dto.*;
import com.project.deporturnos.exception.ContrasenaPocoSeguraException;
import com.project.deporturnos.exception.EmailInvalidoException;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.repository.IUsuarioRepository;
import com.project.deporturnos.service.IUsuarioService;

@Service
public class UsuarioService implements IUsuarioService {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper mapper;


    @Override
    public RegistrationResponseDTO addUsuario(UsuarioDTO usuarioDTO) {

        Usuario usuario = mapper.convertValue(usuarioDTO, Usuario.class);

        // Verificar si el mail ya fué utilizado
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new UserAlreadyExistsException("El email "+ usuario.getEmail()+" ya esta registrado");
        }

        // Validar email
        Pattern patternEmail = Pattern.compile("^(.+)@(.+)$");
        Matcher matcherEmail = patternEmail.matcher(usuario.getEmail());
        if (!matcherEmail.matches()) {
            throw new EmailInvalidoException("El email introducido es inválido.");
        }

        // Validar contraseña: mínimo 8 caracteres, al menos una letra y un número
        Pattern patternPass = Pattern.compile("^(?=.*[a-z])(?=.*?[0-9]).{8,}$");
        Matcher matcherPass = patternPass.matcher(usuario.getPassword());
        if(!matcherPass.matches()) {
            throw new ContrasenaPocoSeguraException("Tu contraseña debe tener como mínimo 8 caracteres, al menos una letra y un número.");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setCuentaActivada(true);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return mapper.convertValue(usuarioGuardado, RegistrationResponseDTO.class);
    }


    @Override
    public LoginMessageDTO loginUsuario(LoginRequestDTO loginDTO) {
        
        //String msg = "";
        Usuario user1 = usuarioRepository.findByEmail(loginDTO.getEmail());
        if(user1 != null){
            String password = loginDTO.getPassword();
            String encodedPassword = user1.getPassword();
            boolean match = passwordEncoder.matches(password, encodedPassword);

            if(match){
                Optional<Usuario> usuario = usuarioRepository.findOneByEmailAndPassword(loginDTO.getEmail(), encodedPassword);

                if(usuario.isPresent()){
                    return new LoginMessageDTO("Login Success", true);
                }else{
                    return new LoginMessageDTO("Login Failed", false);
                }
            }else{
                return new LoginMessageDTO("Contraseña incorrecta", false);
            }
        }else{
            return new LoginMessageDTO("Email inexistente", false);
        }
    }


    @Override
    public Page<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll(Pageable.unpaged());
    }


    @Override
    public void delete(Long id){
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if(usuarioOptional.isPresent()){
            usuarioRepository.deleteById(id);
        }else{
            throw new ResourceNotFoundException("No existe el usuario con el id "+id);
        }
    }


    @Override
    public UsuarioDTO update(Long id, UsuarioRequestUpdateDTO usuarioRequestUpdateDTO){
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if(usuarioOptional.isEmpty()){
            throw new ResourceNotFoundException("No existe el usuario con el id "+id);
        }

        Usuario usuario = usuarioOptional.get();

        if(usuarioRequestUpdateDTO.getNombre() != null){
            usuario.setNombre(usuarioRequestUpdateDTO.getNombre());
        }

        if(usuarioRequestUpdateDTO.getEmail() != null){
            // Validar email
            Pattern patternEmail = Pattern.compile("^(.+)@(.+)$");
            Matcher matcherEmail = patternEmail.matcher(usuarioRequestUpdateDTO.getEmail());
            if (!matcherEmail.matches()) {
                throw new EmailInvalidoException("El email introducido es inválido.");
            }else{
                usuario.setEmail(usuarioRequestUpdateDTO.getEmail());
            }
        }

        if(usuarioRequestUpdateDTO.getPassword() != null){
            // Validar contraseña: mínimo 8 caracteres, al menos una letra y un número
            Pattern patternPass = Pattern.compile("^(?=.*[a-z])(?=.*?[0-9]).{8,}$");
            Matcher matcherPass = patternPass.matcher(usuarioRequestUpdateDTO.getPassword());
            if(!matcherPass.matches()) {
                throw new ContrasenaPocoSeguraException("Tu contraseña debe tener como mínimo 8 caracteres, al menos una letra y un número.");
            }else{
                usuario.setPassword(passwordEncoder.encode(usuarioRequestUpdateDTO.getPassword()));
            }
        }

        if(usuarioRequestUpdateDTO.getTelefono() != null){
            usuario.setTelefono(usuarioRequestUpdateDTO.getTelefono());
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return mapper.convertValue(usuarioGuardado, UsuarioDTO.class);
    }

    

}
