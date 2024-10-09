package com.project.deporturnos.service.implementation;

import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.service.INotificationService;
import com.project.deporturnos.utils.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final EmailService emailService;
    private final AuthService authService;

    public void sendNotificationReservationConfirmed(Usuario user) {
        String qrData = "www.google.com";

        Path tempDirectory = null;
        try {
            tempDirectory = Files.createTempDirectory("qrs");
            System.out.println("Directorio temporal creado: " + tempDirectory.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error al crear el directorio temporal para el QR", e);
        }

        String qrFilePath = tempDirectory.toString() + "/codigoQR_" + user.getId() + ".png";
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
}
