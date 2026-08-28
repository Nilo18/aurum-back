package com.aurum.main.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;

@Data
@Table("product")
public class Product {
    @Id
    private Long id;
    private String productName;
    private String category;
    private BigDecimal price;
    private Long supplierId;
    private BigDecimal quantity;
}
