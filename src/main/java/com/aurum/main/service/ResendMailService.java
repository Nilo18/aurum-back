package com.aurum.main.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.Data;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("production")
@Data
public class ResendMailService implements MailStrategy {

    private final Resend resend;

    public ResendMailService(Resend resend) {
        this.resend = resend;
    }

    @Override
    public void formatAndSend(
            String from,
            String to,
            String subject,
            String message
    ) {
        CreateEmailOptions email = CreateEmailOptions.builder()
                .from(from)
                .to(to)
                .subject(subject)
                .html(message)
                .build();

        try {
            resend.emails().send(email);
        } catch (ResendException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
