package com.aurum.main.service;

import com.aurum.main.dto.MenuItemDTO;
import com.aurum.main.dto.requests.CreateMenuItemRequest;
import com.aurum.main.dto.requests.MenuItemQuery;
import com.aurum.main.dto.requests.UpdateMenuItemRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.exception.MenuItemNotFoundException;
import com.aurum.main.model.MenuItem;
import com.aurum.main.repository.MenuItemRepository;
import com.aurum.main.repository.SearchStrategy;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Data
@Log
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final SearchStrategy<MenuItemDTO, MenuItemQuery> searchStrategy;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public List<MenuItem> getMenuItems(MenuItem.CuisineType cuisineType) {
        if (cuisineType == MenuItem.CuisineType.GENERAL) {
            return jdbcAggregateTemplate.findAll(MenuItem.class);
        }

        Criteria criteria = Criteria.where("cuisine_type")
                .in(cuisineType, MenuItem.CuisineType.GENERAL);

        Query query = Query.query(criteria);

        return jdbcAggregateTemplate.findAll(query, MenuItem.class);
    }

    public PageResponse<MenuItemDTO> getMenuItems(MenuItemQuery query) {
        return searchStrategy.search(query);
    }

    public MenuItemDTO addMenuItem(CreateMenuItemRequest data) {
        MenuItem menuItem = new MenuItem();
        String publicId = UUID.randomUUID().toString();

        menuItem.setPublicId(publicId);
        menuItem.setName(data.name());
        menuItem.setCategory(data.category());
        menuItem.setType(data.type());
        menuItem.setPricePerPerson(data.pricePerPerson());
        menuItemRepository.save(menuItem);
        log.info("SAVED THE MENU ITEM");

        return new MenuItemDTO(
                publicId,
                data.name(),
                data.category(),
                data.type(),
                data.pricePerPerson()
        );
    }

    public GenericResponse deleteMenuItem(String publicId) {
        int deleted = menuItemRepository.deleteByPublicId(publicId);

        if (deleted == 0) {
            throw new MenuItemNotFoundException("Menu item not found");
        }

        return new GenericResponse(200, "Menu item deleted successfully");
    }

    public MenuItemDTO updateMenuItem(UpdateMenuItemRequest updatedMenuItem) {
        MenuItem menuItem = menuItemRepository.findByPublicId(updatedMenuItem.publicId())
                .orElseThrow(() -> new MenuItemNotFoundException("Menu item not found"));

        menuItem.setName(updatedMenuItem.name());
        menuItem.setCategory(updatedMenuItem.category());
        menuItem.setType(updatedMenuItem.type());
        menuItem.setPricePerPerson(updatedMenuItem.pricePerPerson());
        menuItemRepository.save(menuItem);

        return new MenuItemDTO(
                menuItem.getPublicId(),
                menuItem.getName(),
                menuItem.getCategory(),
                menuItem.getType(),
                menuItem.getPricePerPerson()
        );
    }
}
