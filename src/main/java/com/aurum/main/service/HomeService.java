package com.aurum.main.service;

import com.aurum.main.dto.requests.ContactRequest;
import lombok.Data;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Data
public class HomeService {
    private final JavaMailSender mailSender;

    public void sendContactEmail(ContactRequest request) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(request.emailAddress());
        mailMessage.setTo("nikalongurashvili@gmail.com");
        mailMessage.setSubject("Aurum Question");
        mailMessage.setText(
                request.message() + "\nName: " + request.fullName() +
                "\nPhone Number: " + request.phoneNumber()
        );
        mailSender.send(mailMessage);
    }
}
