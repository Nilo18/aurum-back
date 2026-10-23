package com.aurum.main.controller;

import com.aurum.main.dto.MenuItemDTO;
import com.aurum.main.dto.requests.MenuItemQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.model.MenuItem;
import com.aurum.main.service.MenuItemService;
import com.aurum.main.service.MenuService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/menu")
@Data
public class MenuItemController {
    private final MenuItemService menuItemService;

    @GetMapping
    public ResponseEntity<PageResponse<MenuItemDTO>> getMenuItems(@ModelAttribute MenuItemQuery query) {
        return ResponseEntity.ok(menuItemService.getMenuItems(query));
    }
}
