package com.aurum.main.dto;

import com.aurum.main.model.Event;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventDTO(
        String clientName,
        Event.EventType eventType,
        LocalDate date,
        BigDecimal totalCost,
        Integer guestCount,
        Event.Location location,
        Event.EventStatus status
) {
}
