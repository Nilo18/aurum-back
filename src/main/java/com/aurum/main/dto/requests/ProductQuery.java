package com.aurum.main.dto.requests;

import com.aurum.main.model.Product;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductQuery {
    private Integer page = 0;
    private Integer size = 10;
    private Product.ProductCategory productCategory;
    private BigDecimal priceFrom;
    private BigDecimal priceTo;
    private Long supplierId;
    private Integer quantityFrom;
    private Integer quantityTo;
    private String sortBy = "";
    private String sortDirection = "";
    private String search = "";
}