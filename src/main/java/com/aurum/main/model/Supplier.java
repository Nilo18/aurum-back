package com.aurum.main.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("supplier")
public class Supplier {
    @Id
    private Long id;
    private String partnerNumber;
    private String productType;
    private String city;
    private String street;
    private String number;
}
