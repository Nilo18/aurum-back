package com.aurum.main.dto.requests;

import com.aurum.main.model.MenuItem;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateMenuItemRequest(
        @NotBlank(message = "Menu item name is required")
        @Size(max = 150, message = "Menu item name must not exceed 150 characters")
        String name,
        @NotNull(message = "Menu item category is required")
        MenuItem.MenuItemCategory category,
        @NotNull(message = "Cuisine type is required")
        MenuItem.CuisineType type,
        @NotNull(message = "Price per person is required")
        @PositiveOrZero(message = "Price per person cannot be negative")
        @Digits(integer = 8, fraction = 2, message = "Price per person must have at most 8 integer digits and 2 decimal places")
        BigDecimal pricePerPerson
) {
}
