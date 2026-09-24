package com.aurum.main.dto;

import com.aurum.main.model.Event;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardEventDTO(
        Event.EventType eventType,
        LocalDate date,
        Integer guestCount,
        String clientName
) {
}
