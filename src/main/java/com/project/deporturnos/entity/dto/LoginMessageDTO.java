package com.project.deporturnos.entity.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginMessageDTO {

    private String message;
    private Boolean status;

    @Override
    public String toString() {
        return "LoginMessageDTO [message=" + message + ", status=" + status + "]";
    }
}
