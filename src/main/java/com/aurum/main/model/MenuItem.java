package com.aurum.main.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("menu_items")
@Data
public class MenuItem {
    @Id
    private Long id;
    private String name;
    private MenuItemCategory category;
    private BigDecimal pricePerPerson;

    public enum MenuItemCategory {
        APPETIZER,
        MAIN_COURSE,
        DESSERT,
        DRINK
    }

    public enum CuisineType {
        GEORGIAN,
        MEDITERRANEAN,
        GENERAL
    }
}
