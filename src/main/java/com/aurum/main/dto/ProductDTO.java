package com.aurum.main.dto;

import java.math.BigDecimal;

public record ProductDTO(
        String publicId,
        String productName,
        String category,
        BigDecimal price,
        Long supplierId,
        Integer quantity
) {
}
