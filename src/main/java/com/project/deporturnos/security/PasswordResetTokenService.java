package com.project.deporturnos.security;

import com.project.deporturnos.entity.domain.Usuario;
import com.project.deporturnos.exception.ResourceNotFoundException;
import com.project.deporturnos.service.implementation.EmailService;
import com.project.deporturnos.service.implementation.UsuarioService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.MessagingException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PasswordResetTokenService {
    @Value("${security.jwt.secret-key}")
    private String SECRET_KEY;

    @Getter
    @Value("${security.jwt.expiration-time}")
    private long jwtExpirationTime;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioService usuarioService;

    public String generateToken(String email, List<String> roles) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationTime))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)), SignatureAlgorithm.HS256)
                .claim("roles", roles)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
                    .build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public void sendResetToken(String email, String token) {
        String subject = "Restablecimiento de Contraseña";
        String resetUrl = "http://localhost:5173/client-resetpassword?token=" + token;
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Restablecimiento de Contraseña</h2>"
                + "<p style=\"font-size: 16px;\">Haga clic en el siguiente enlace para restablecer su contraseña.</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<a href=\"" + resetUrl + "\" style=\"font-size: 18px; font-weight: bold; color: #007bff; text-decoration: none;\">Click Aquí</a>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
        try {
            emailService.sendEmail(email, subject, htmlMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo de restablecimiento de contraseña.");
        }
    }

    public Usuario getUserByToken(String token){
        String email = jwtService.getUsernameFromToken(token);
        return usuarioService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Token inválido o expirado."));
    }
}
