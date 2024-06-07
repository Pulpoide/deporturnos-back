package com.project.deporturnos.service.implementation;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.LoginResponseDTO;
import com.project.deporturnos.entity.dto.LoginMessageDTO;
import com.project.deporturnos.entity.dto.UsuarioDTO;
import com.project.deporturnos.repository.IUsuarioRepository;
import com.project.deporturnos.service.IUsuarioService;

@Service
public class UsuarioService implements IUsuarioService{

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public String addUsuario(UsuarioDTO usuarioDTO) {

        Usuario usuario = new Usuario(
            usuarioDTO.getId(),
            usuarioDTO.getNombre(),
            usuarioDTO.getEmail(),
            this.passwordEncoder.encode(usuarioDTO.getPassword())
        );

        usuarioRepository.save(usuario);
        return usuario.getNombre();
    }


    @Override
    public LoginMessageDTO loginUsuario(LoginResponseDTO loginDTO) {
        
        //String msg = "";
        Usuario user1 = usuarioRepository.findByEmail(loginDTO.getEmail());
        if(user1 != null){
            String password = loginDTO.getPassword();
            String encodedPassword = user1.getPassword();
            Boolean isPwdRight = passwordEncoder.matches(password, encodedPassword);

            if(isPwdRight){
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

    

}
