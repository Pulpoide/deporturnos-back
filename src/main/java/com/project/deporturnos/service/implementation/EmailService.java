package com.project.deporturnos.service.implementation;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    private final JavaMailSender emailSender;

    @Async
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        if (to == null || subject == null || body == null) {
            log.warn("❌ Email no enviado: parámetros nulos (to={}, subject={})", to, subject);
            return;
        }

        if (!mailEnabled) {
            log.warn("[TEST MODE] Email not sent to {} - subject: {}", to, subject);
            return;
        }

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        emailSender.send(message);
    }

    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath)
            throws MessagingException {
        if (to == null || subject == null || body == null) {
            log.warn("❌ Email no enviado: parámetros nulos (to={}, subject={})", to, subject);
            return;
        }

        if (!mailEnabled) {
            log.warn("[TEST MODE] Email not sent to {} - subject: {}", to, subject);
            return;
        }

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        // Adjuntar archivo QR al correo
        FileSystemResource file = new FileSystemResource(new File(attachmentPath));
        helper.addAttachment("codigoQR.png", file);

        emailSender.send(message);
    }
}
