package com.aurum.main.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTests {

    @Mock org.springframework.mail.javamail.JavaMailSender sender;
    @InjectMocks
    SmtpMailService service;

    @Test void sendsAllMessageFields() {
        service.formatAndSend("from@example.com", "to@example.com", "Subject", "Body");
        var message = org.mockito.ArgumentCaptor.forClass(org.springframework.mail.SimpleMailMessage.class);
        verify(sender).send(message.capture());
        assertEquals("from@example.com", message.getValue().getFrom());
        assertArrayEquals(new String[]{"to@example.com"}, message.getValue().getTo());
        assertEquals("Subject", message.getValue().getSubject());
        assertEquals("Body", message.getValue().getText());
    }

    @Test void deliveryFailureIsPropagated() {
        var failure = new org.springframework.mail.MailSendException("Unavailable");
        doThrow(failure).when(sender).send(any(org.springframework.mail.SimpleMailMessage.class));
        assertSame(failure, assertThrows(org.springframework.mail.MailSendException.class,
                () -> service.formatAndSend("from@example.com", "to@example.com", "Subject", "Body")));
    }

}
