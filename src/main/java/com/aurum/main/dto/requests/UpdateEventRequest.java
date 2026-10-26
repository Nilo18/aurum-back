package com.aurum.main.dto.requests;

import com.aurum.main.model.Event;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UpdateEventRequest(
        @NotBlank(message = "Event public ID is required")
        String publicId,
        @NotNull(message = "Event type is required")
        Event.EventType eventType,
        @NotNull(message = "Event date is required")
        LocalDate date,
        @NotNull(message = "Total cost is required")
        @PositiveOrZero(message = "Total cost cannot be negative")
        @Digits(integer = 10, fraction = 2, message = "Total cost must have at most 10 integer digits and 2 decimal places")
        BigDecimal totalCost,
        @NotNull(message = "Guest count is required")
        @PositiveOrZero(message = "Guest count cannot be negative")
        Integer guestCount,
        @NotNull(message = "Location is required")
        Event.Location location,
        @NotNull(message = "Event status is required")
        Event.EventStatus status
//        @NotNull(message = "Dishes from the menu must be selected")
//        List<@NotBlank(message = "Menu item public ID is required") String> menuItemPublicIds
) {
}
