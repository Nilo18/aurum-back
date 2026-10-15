package com.aurum.main.dto.requests;

import com.aurum.main.model.Product;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name must not exceed 200 characters")
        String productName,
        @NotNull(message = "Product category is required")
        Product.ProductCategory category,
        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price cannot be negative")
        @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
        BigDecimal price,
        @NotBlank(message = "Supplier public ID is required")
        String supplierPublicId,
        @NotNull(message = "Quantity is required")
        @PositiveOrZero(message = "Quantity cannot be negative")
        Integer quantity
) {
}
