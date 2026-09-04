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
    private final JavaMailSender mailSender;
    private final OtpService otpService;

    public OtpResponse sendContactRequestVerification(EmailRequest emailReq) {
        OtpResponse otpRes = otpService.generateOtp();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("nikalongurashvili@gmail.com");
        mailMessage.setTo(emailReq.email());
        mailMessage.setSubject("Contact Request Verification");
        mailMessage.setText(
            "Hello, here's your contact request verification code: " + otpRes.otp()
        );
        mailSender.send(mailMessage);
        return new OtpResponse(otpRes.transactionKey(), otpRes.otp());
    }

    public GenericResponse sendContactEmail(ContactRequest request) {
        boolean otpIsValid = otpService.validateOtp(request.transactionKey(), request.otp());

        if (!otpIsValid) {
            throw new InvalidOtpException("Invalid verification code");
        }

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(request.emailAddress());
        mailMessage.setTo("nikalongurashvili@gmail.com");
        mailMessage.setSubject("Aurum Question");
        mailMessage.setText(
                request.message() + "\nName: " + request.fullName() +
                "\nPhone Number: " + request.phoneNumber()
        );
        mailSender.send(mailMessage);

        return new GenericResponse(200, "Contact received successfully");
    }
}
