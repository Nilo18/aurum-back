package com.aurum.main.dto.requests;

import com.aurum.main.model.Product;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SupplierQuery {
    private Integer page = 0;
    private Integer size = 10;
    private Product.ProductCategory productType;
    private String sortBy = "";
    private String sortDirection = "";
    private String search = "";
}
