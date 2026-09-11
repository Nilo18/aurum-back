package com.aurum.main.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record InvitationTokenRequest(
        @NotBlank(message = "Invitation token is required")
        String token
) {
}
