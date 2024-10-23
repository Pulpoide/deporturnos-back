package com.project.deporturnos.service.implementation;

import com.project.deporturnos.entity.domain.Turno;
import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.service.INotificationService;
import com.project.deporturnos.utils.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final EmailService emailService;

    public void sendNotificationReservationConfirmed(Usuario user, Long reservaId) {
        String qrData = "http://localhost:5173/validate-reserva/" + reservaId;

        Path tempDirectory = null;
        try {
            tempDirectory = Files.createTempDirectory("qrs");
            System.out.println("Directorio temporal creado: " + tempDirectory.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error al crear el directorio temporal para el QR", e);
        }

        String qrFilePath = tempDirectory + "/codigoQR_" + user.getId() + ".png";
        System.out.println("Ruta del archivo QR: " + qrFilePath);

        try {
            // Generar el QR
            QRCodeGenerator.generateQRCodeImage(qrData, 200, 200, qrFilePath);

            File qrFile = new File(qrFilePath);
            if (!qrFile.exists()) {
                throw new RuntimeException("El archivo QR no fue creado: " + qrFilePath);
            }

            // Enviar email con el archivo adjunto
            String subject = "Reserva Confirmada ⚽";
            String body = "<html>"
                    + "<body style=\"font-family: Arial, sans-serif;\">"
                    + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                    + "<h2 style=\"color: #333;\">¡Gracias por reservar tu cancha con DeporTurnos!</h2>"
                    + "<p style=\"font-size: 16px;\">Muestre el siguiente código de reserva al canchero para acceder a la cancha.</p>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            emailService.sendEmailWithAttachment(user.getEmail(), subject, body, qrFilePath);

            // Eliminar el archivo QR después de enviar el correo
            if (qrFile.exists()) {
                qrFile.delete();
                System.out.println("Archivo QR eliminado después de enviar el correo.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al generar o enviar el código QR", e);
        }
    }

    public void notifyUsersAboutPromotions(List<Usuario> usuariosANotificar, Turno turno, Usuario usuarioQueCancela){
        String subject = "Turno disponible en promoción ⚽";

        String url = "http://localhost:5173/turnos-disponibles/" + turno.getCancha().getId();

        String body = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">¡Turno " + turno.getId() + " disponible!</h2>"                + "<p style=\"font-size: 16px;\">Se ha liberado un turno en la cancha: " + turno.getCancha().getNombre() + ".</p>"
                + "<p style=\"font-size: 16px;\">Fecha: " + turno.getFecha() + "</p>"
                + "<p style=\"font-size: 16px;\">Hora de inicio: " + turno.getHoraInicio() + "</p>"
                + "<p style=\"font-size: 16px;\">Hora de fin: " + turno.getHoraFin() + "</p>"
                + "<p style=\"font-size: 16px;\">¡Aprovecha el descuento especial por cancelación!</p>"
                + "<a href=\"" + url + "\" style=\"display: inline-block; background-color: #4CAF50; color: white; padding: 10px 20px; text-align: center; text-decoration: none; font-size: 16px; border-radius: 5px;\">Ver turno disponible</a>"
                + "</div>"
                + "</body>"
                + "</html>";

        // Excluimos al usuario que realizó la cancelación
        List<Usuario> usuariosFiltrados = usuariosANotificar.stream()
                .filter(u -> !u.getId().equals(usuarioQueCancela.getId()))
                .toList();

        // Iteramos sobre los usuarios a notificar
        for (Usuario usuario : usuariosFiltrados) {
            try {
                emailService.sendEmail(usuario.getEmail(), subject, body);
                System.out.println("Notificación enviada a: " + usuario.getEmail());
            } catch (Exception e) {
                System.err.println("Error al enviar notificación a: " + usuario.getEmail());
                e.printStackTrace();
            }
        }
    }
}
