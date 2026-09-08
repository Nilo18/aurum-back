package com.aurum.main.controller;

import com.aurum.main.model.MenuItem;
import com.aurum.main.service.MenuService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/menu")
@Data
public class MenuController {
    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuItem>> getMenuItems(
            @RequestParam(defaultValue = "GENERAL") MenuItem.CuisineType cuisineType
    ) {
        return ResponseEntity.ok(menuService.getMenuItems(cuisineType));
    }
}
