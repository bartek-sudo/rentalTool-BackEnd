package com.example.rentalTool_BackEnd.user.service.impl;

import com.example.rentalTool_BackEnd.user.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;
    
    @Value("${spring.mail.username:noreply@rentaltool.com}")
    private String fromEmail;
    
    @Override
    public void sendVerificationEmail(String to, String token, String firstName) {
        // Link wskazuje na backend API, który zweryfikuje email
        String verificationUrl = backendUrl + "/api/v1/auth/verify-email?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Potwierdź swój adres email - RentalTool");
        message.setText(buildEmailContent(firstName, verificationUrl));
        
        mailSender.send(message);
    }
    
    private String buildEmailContent(String firstName, String verificationUrl) {
        return "Witaj " + firstName + "!\n\n" +
                "Dziękujemy za rejestrację w RentalTool.\n\n" +
                "Aby potwierdzić swój adres email, kliknij w poniższy link:\n" +
                verificationUrl + "\n\n" +
                "Link jest ważny przez 24 godziny.\n\n" +
                "Jeśli nie rejestrowałeś się w RentalTool, zignoruj tę wiadomość.\n\n" +
                "Pozdrawiamy,\n" +
                "Zespół RentalTool";
    }
}

