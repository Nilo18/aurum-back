package com.aurum.main.service;

import com.aurum.main.dto.requests.ContactRequest;
import com.aurum.main.dto.requests.EmailRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.OtpResponse;
import com.aurum.main.exception.InvalidOtpException;
import com.aurum.main.utils.OtpPurpose;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

@Service
@Data
@Log
public class HomeService {
    private final MailStrategy mailStrategy;
    private final OtpService otpService;

    public OtpResponse sendContactRequestVerification(EmailRequest emailReq) {
        var otpRes = otpService.generateOtp(emailReq.email(), OtpPurpose.CONTACT);

        mailStrategy.formatAndSend(
                "nikalongurashvili@gmail.com",
                emailReq.email(),
                "Contact Request Verification",
                "Hello, here's your contact request verification code: " + otpRes.otp()
        );

        return new OtpResponse(otpRes.transactionKey());
    }

    public GenericResponse sendContactEmail(ContactRequest request) {
        boolean otpIsValid = otpService.validateOtp(
                request.transactionKey(), request.emailAddress(), OtpPurpose.CONTACT, request.otp());

        if (!otpIsValid) {
            throw new InvalidOtpException("Invalid verification code");
        }

        mailStrategy.formatAndSend(
                request.emailAddress(),
                "nikalongurashvili@gmail.com",
                "Aurum Question",
                request.message() + "\nName: " + request.fullName() +
                        "\nPhone Number: " + request.phoneNumber() +
                        "\nEmail: " + request.emailAddress()
        );


        return new GenericResponse(200, "Contact received successfully");
    }
}
