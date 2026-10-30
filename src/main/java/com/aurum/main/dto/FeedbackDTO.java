package com.aurum.main.dto;

import java.time.LocalDate;

public record FeedbackDTO(
        String publicId,
        String author,
        LocalDate commentDate,
        Short rating,
        String description
) {
}
