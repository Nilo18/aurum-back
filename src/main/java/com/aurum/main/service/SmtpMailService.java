package com.aurum.main.service;

import lombok.Data;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Data
@Profile("local")
public class SmtpMailService implements MailStrategy {
    private final JavaMailSender mailSender;

    @Override
    public void formatAndSend(String from, String to, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(from);
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(
                message
        );
        mailSender.send(mailMessage);
    }
}
