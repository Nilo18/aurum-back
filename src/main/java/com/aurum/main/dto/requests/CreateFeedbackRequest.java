package com.aurum.main.dto.requests;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateFeedbackRequest(
        @NotBlank
        String tokenHash,

        @NotBlank
        @Size(max = 100)
        String author,

//        @NotNull
//        @PastOrPresent
//        LocalDate commentDate,

        @NotNull
        @Min(1)
        @Max(5)
        Short rating,

        @NotBlank
        @Size(max = 2000)
        String description
) {
}