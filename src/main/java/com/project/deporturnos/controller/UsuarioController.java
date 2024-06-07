package com.project.deporturnos.controller;

import org.springframework.web.bind.annotation.RestController;

import com.project.deporturnos.entity.dto.LoginResponseDTO;
import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.entity.dto.LoginMessageDTO;
import com.project.deporturnos.entity.dto.UsuarioDTO;
import com.project.deporturnos.service.IUsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;




@RestController
@CrossOrigin
@RequestMapping("api/v1/usuario")
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;


    @GetMapping(path="/all")
    public ResponseEntity<Page<Usuario>> getAllUsuarios(
        // @RequestParam(required = false, defaultValue = "0") Integer page, 
        // @RequestParam(required = false, defaultValue = "10") Integer size,
        // @RequestParam(required = false, defaultValue = "false") Boolean enablePagination
        ){

        return ResponseEntity.ok(usuarioService.getAllUsuarios());
    }


    @PostMapping(path = "/save")
    public String saveUsuario(@RequestBody UsuarioDTO usuarioDTO) {

        String id = usuarioService.addUsuario(usuarioDTO);
        return id;
    }

    @PostMapping(path="/login")
    public ResponseEntity<?> loginUsuario(@RequestBody LoginResponseDTO loginDTO) {
        LoginMessageDTO loginMessage = usuarioService.loginUsuario(loginDTO);
        return ResponseEntity.ok(loginMessage);
    }
    

    
    // @DeleteMapping(value = "/{id}")
    // public ResponseEntity<Boolean> deleteUsuario(@PathVariable("id") Long id){
    //     usuarioService.deleteUsuario(id);
    //     return ResponseEntity.ok(usuarioService.existsById(id));
    // }

    // @GetMapping(value = "/{id}")
    // public ResponseEntity<Optional<Usuario>> findUsuarioById(@PathVariable("id") Long id){
    //     return ResponseEntity.status(HttpStatus.OK).body(usuarioService.findById(id));
    // }
    

    // @PutMapping
    // public ResponseEntity<Usuario> editUsuario(@Valid @RequestBody Usuario user) {
    //     return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.editUsuario(user));
    // }

    
}
