package com.project.deporturnos.entity.dto;

public class LoginResponseDTO {

    String email;

    String password;

    public LoginResponseDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginResponseDTO() {
        
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

    @Override
    public String toString() {
        return "LoginDTO [email=" + email + ", password=" + password + "]";
    }
    
}
