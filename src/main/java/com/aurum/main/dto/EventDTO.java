package com.aurum.main.dto;

import com.aurum.main.model.Event;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventDTO(
        @NotNull(message = "Event type is required")
        Event.EventType eventType,
        @NotNull(message = "Event date is required")
        LocalDate date,
        BigDecimal totalCost,
        @NotNull(message = "Guest count is required")
        Integer guestCount,
        @NotNull(message = "Location is required")
        Event.Location location,
        String notes
) {
}
