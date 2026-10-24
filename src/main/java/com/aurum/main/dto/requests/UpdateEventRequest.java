package com.aurum.main.dto.requests;

import com.aurum.main.dto.ClientDTO;
import com.aurum.main.model.Event;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UpdateEventRequest(
        String publicId,
        Event.EventType eventType,
        LocalDate date,
        BigDecimal totalCost,
        Integer guestCount,
        Event.Location location,
        Event.EventStatus status,
        List<String> menuItemPublicIds
) {
}
