package com.aurum.main.dto;

import com.aurum.main.model.MenuItem;

import java.math.BigDecimal;

public record MenuItemDTO(
        String publicId,
        String name,
        MenuItem.MenuItemCategory category,
        MenuItem.CuisineType type,
        BigDecimal pricePerPerson
) {
}
