package com.aurum.main.controller;

import com.aurum.main.dto.MenuItemDTO;
import com.aurum.main.dto.requests.CreateMenuItemRequest;
import com.aurum.main.dto.requests.MenuItemQuery;
import com.aurum.main.dto.requests.UpdateMenuItemRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.model.MenuItem;
import com.aurum.main.service.MenuItemService;
import com.aurum.main.service.MenuService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/menu")
@Data
public class MenuItemController {
    private final MenuItemService menuItemService;

    @GetMapping(path = "/request-event")
    public ResponseEntity<List<MenuItem>> getMenuItemsForRequestEventPage(
            @RequestParam(defaultValue = "GENERAL") MenuItem.CuisineType cuisineType
    ) {
        return ResponseEntity.ok(menuItemService.getMenuItems(cuisineType));
    }

    @GetMapping
    public ResponseEntity<PageResponse<MenuItemDTO>> getMenuItems(@ModelAttribute MenuItemQuery query) {
        return ResponseEntity.ok(menuItemService.getMenuItems(query));
    }

    @PostMapping
    public ResponseEntity<MenuItemDTO> addMenuItem(@Valid @RequestBody CreateMenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.addMenuItem(request));
    }

    @PutMapping
    public ResponseEntity<MenuItemDTO> updateMenuItem(@Valid @RequestBody UpdateMenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.updateMenuItem(request));
    }

    @DeleteMapping(path = "/{publicId}")
    public ResponseEntity<GenericResponse> deleteMenuItem(@PathVariable String publicId) {
        return ResponseEntity.ok(menuItemService.deleteMenuItem(publicId));
    }
}