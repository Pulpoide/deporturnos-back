package com.project.deporturnos.entity.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String rol;

    @Column(name = "cuenta_activada", nullable = false)
    private boolean cuentaActivada;

    @Column(nullable = true)
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Número de teléfono no válido")
    private String telefono;

    // Constructor
    public Usuario(int id, String nombre, String email, String encode) {
    }

    public Usuario() {

    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isCuentaActivada() {
        return cuentaActivada;
    }

    public void setCuentaActivada(boolean cuentaActivada) {
        this.cuentaActivada = cuentaActivada;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}

