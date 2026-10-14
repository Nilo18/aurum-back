package com.aurum.main.dto;

import com.aurum.main.model.Product;

import java.math.BigDecimal;

public record ProductRowDTO(
        String publicId,
        String productName,
        Product.ProductCategory category,
        BigDecimal price,
        ProductSectionSupplierDTO supplier,
        Integer quantity
) {
}
