package com.aurum.main.dto.requests;

import com.aurum.main.model.MenuItem;
import com.aurum.main.model.Vehicle;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuItemQuery {
    private Integer page = 0;
    private Integer size = 10;
    private MenuItem.MenuItemCategory category;
    private MenuItem.CuisineType type;
    private BigDecimal priceFrom;
    private BigDecimal priceTo;
    private String sortBy = "";
    private String sortDirection = "";
}
