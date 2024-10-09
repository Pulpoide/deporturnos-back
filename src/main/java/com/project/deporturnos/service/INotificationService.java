package com.project.deporturnos.service;

import com.project.deporturnos.entity.domain.Usuario;

public interface INotificationService {
    void sendNotificationReservationConfirmed(Usuario user);
}
