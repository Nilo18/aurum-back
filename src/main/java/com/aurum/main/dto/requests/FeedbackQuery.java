package com.aurum.main.dto.requests;

import com.aurum.main.model.MenuItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FeedbackQuery {
    private Integer page = 0;
    private Integer size = 10;
    @Min(1)
    @Max(5)
    private Short ratingFrom;
    @Min(1)
    @Max(5)
    private Short ratingTo;
    private LocalDate commentFrom;
    private LocalDate commentTo;
    private String search = "";
    private String sortBy = "";
    private String sortDirection = "";
}
