package com.aurum.main.dto;

import com.aurum.main.model.Product;

public record SupplierCategoryDTO(
        String supplierPublicId,
        Product.ProductCategory category
) {
}
