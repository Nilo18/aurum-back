package com.aurum.main.dto;

import com.aurum.main.annotations.ValidPhone;
import com.aurum.main.model.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClientDTO(
        @NotNull(message = "Client type is required")
        Client.ClientType clientType,
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Email is required")
        @Email
        String email,
        @NotBlank(message = "Phone number is required")
        @ValidPhone(message = "Phone number must be valid")
        String phone
) {
}
