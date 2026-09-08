package com.aurum.main.service;

import com.aurum.main.dto.requests.ContactRequest;
import com.aurum.main.dto.requests.EmailRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.OtpResponse;
import com.aurum.main.exception.InvalidOtpException;
import lombok.Data;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Data
public class HomeService {
    private final MailService mailService;
    private final OtpService otpService;

    public OtpResponse sendContactRequestVerification(EmailRequest emailReq) {
        OtpResponse otpRes = otpService.generateOtp();

        mailService.formatAndSend(
                "nikalongurashvili@gmail.com",
                emailReq.email(),
                "Contact Request Verification",
                "Hello, here's your contact request verification code: " + otpRes.otp()
        );

        return new OtpResponse(otpRes.transactionKey(), otpRes.otp());
    }

    public GenericResponse sendContactEmail(ContactRequest request) {
        boolean otpIsValid = otpService.validateOtp(request.transactionKey(), request.otp());

        if (!otpIsValid) {
            throw new InvalidOtpException("Invalid verification code");
        }

        mailService.formatAndSend(
                request.emailAddress(),
                "nikalongurashvili@gmail.com",
                "Aurum Question",
                request.message() + "\nName: " + request.fullName() +
                        "\nPhone Number: " + request.phoneNumber()
        );

        otpService.invalidateOtp(request.transactionKey());

        return new GenericResponse(200, "Contact received successfully");
    }
}
