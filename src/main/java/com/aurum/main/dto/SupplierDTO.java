package com.aurum.main.dto;

import com.aurum.main.model.Product;

import java.util.Set;

public record SupplierDTO(
        String publicId,
        String name,
        Set<Product.ProductCategory> categories,
        String city,
        String street,
        String number
) {
}
