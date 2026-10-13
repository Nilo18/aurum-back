package com.aurum.main.dto.requests;

import com.aurum.main.model.Product;

import java.math.BigDecimal;

public record CreateProductRequest(
        String productName,
        Product.ProductCategory category,
        BigDecimal price,
        Long supplierId,
        Integer quantity
) {
}
