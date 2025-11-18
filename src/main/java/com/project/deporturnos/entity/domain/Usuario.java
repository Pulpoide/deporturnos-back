package com.project.deporturnos.entity.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql="UPDATE usuario SET deleted = true WHERE id=?")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Email(message = "Correo electrónico no válido.")
    @Column(nullable = false, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column
    private String telefono;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Column(name = "cuenta_activada", nullable = false)
    private boolean activada;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_expire_date")
    private LocalDateTime verificationCodeExpiresAt;

    @Column
    @Builder.Default
    private boolean deleted = Boolean.FALSE;

    @Column(name = "notificaciones", nullable = false)
    @Builder.Default
    private boolean notificaciones = Boolean.FALSE;

    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private Set<Reserva> reservas;

    public Usuario(Long id, String nombre, String email, String pass, String tel, Rol rol, boolean activate) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = pass;
        this.telefono = tel;
        this.rol = rol;
        this.activada = activate;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.rol.name()));
    }

    @Override
    public String getUsername() {
        return this.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActivada();
    }
}

