package com.aurum.main.service;

import com.aurum.main.dto.*;
import com.aurum.main.dto.requests.*;
import com.aurum.main.dto.responses.*;
import com.aurum.main.exception.*;
import com.aurum.main.model.*;
import com.aurum.main.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class HomeServiceTests {

    @Mock MailService mail;
    @Mock OtpService otp;
    @InjectMocks HomeService service;
    private final ContactRequest request = new ContactRequest("Alice", "+995555123456",
            "alice@example.com", "Question", "transaction", "123456");

    @Test void sendsVerificationCode() {
        var response = new OtpResponse("transaction", "123456");
        when(otp.generateOtp()).thenReturn(response);
        assertEquals(response, service.sendContactRequestVerification(new EmailRequest(request.emailAddress())));
        verify(mail).formatAndSend(anyString(), eq(request.emailAddress()), anyString(), contains(response.otp()));
    }

    @Test void invalidOtpDoesNotSendContactMessage() {
        assertThrows(InvalidOtpException.class, () -> service.sendContactEmail(request));
        verifyNoInteractions(mail);
    }

    @Test void validContactSendsContactDetailsAndInvalidatesOtp() {
        when(otp.validateOtp("transaction", "123456")).thenReturn(true);
        assertEquals(new GenericResponse(200, "Contact received successfully"), service.sendContactEmail(request));
        verify(mail).formatAndSend(eq(request.emailAddress()), anyString(), eq("Aurum Question"),
                eq("Question\nName: Alice\nPhone Number: +995555123456"));
        verify(otp).invalidateOtp("transaction");
    }

}
