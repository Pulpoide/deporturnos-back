package com.project.deporturnos.entity.dto;

import lombok.Data;

@Data
public class LoginMessageDTO {

    String message;
    Boolean status;

    //Constructor
    public LoginMessageDTO(String message, Boolean status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage(String message){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public void setStatus(Boolean status){
        this.status = status;
    }

    @Override
    public String toString() {
        return "LoginMessageDTO [message=" + message + ", status=" + status + "]";
    }

}
