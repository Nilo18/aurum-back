package com.aurum.main.dto;

import com.aurum.main.model.Product;

import java.math.BigDecimal;

public record ProductDTO(
        String publicId,
        String productName,
        Product.ProductCategory category,
        BigDecimal price,
        Long supplierId,
        Integer quantity
) {
}
