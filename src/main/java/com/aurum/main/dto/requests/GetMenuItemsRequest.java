package com.aurum.main.dto.requests;

import com.aurum.main.model.MenuItem;

public record GetMenuItemsRequest(MenuItem.CuisineType cuisineType) {
}
