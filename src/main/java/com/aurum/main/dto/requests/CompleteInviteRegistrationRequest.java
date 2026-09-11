package com.aurum.main.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteInviteRegistrationRequest(
        @NotBlank(message = "Invitation token is required")
        String token,
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be 255 characters long at most")
        String name,
        @NotBlank(message = "Password is required")
        String password
) {
}
