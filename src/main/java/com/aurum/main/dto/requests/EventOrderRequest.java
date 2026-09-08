package com.aurum.main.dto.requests;

import com.aurum.main.dto.ClientDTO;
import com.aurum.main.dto.EventDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EventOrderRequest(
        @Valid
        @NotNull(message = "Client info is required")
        ClientDTO client,
        @Valid
        @NotNull(message = "Event info is required")
        EventDTO event,
        @NotNull(message = "Dishes from the menu must be selected")
        List<Long> menuItemIds,
        @NotBlank(message = "Transaction key is required")
        String transactionKey,
        @NotBlank(message = "Otp is required")
        String otp
) {
}
