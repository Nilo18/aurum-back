package com.aurum.main.dto.requests;

import com.aurum.main.model.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateSupplierRequest(
        @NotBlank
        String publicId,

        @NotBlank
        @Size(max = 50)
        String name,

        @NotBlank
        @Size(max = 100)
        String city,

        @NotBlank
        @Size(max = 200)
        String street,

        @NotBlank
        @Size(max = 30)
        String number,

        @NotEmpty
        Set<Product.ProductCategory> categories
) {
}
