package com.aurum.main.dto.requests;


import com.aurum.main.annotations.ValidPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be 255 characters long at most")
        String fullName,
        @NotBlank(message = "Phone number is required")
        @ValidPhone(message = "Phone number must be valid")
        String phoneNumber,
        @NotBlank(message = "Email is required")
        @Email
        String emailAddress,
        @NotBlank(message = "Message is required")
        String message,
        @NotBlank(message = "Transaction key is required")
        String transactionKey,
        @NotBlank(message = "Otp is required")
        String otp
) {
}
