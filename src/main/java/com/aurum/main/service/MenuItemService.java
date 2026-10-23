package com.aurum.main.service;

import com.aurum.main.dto.MenuItemDTO;
import com.aurum.main.dto.requests.MenuItemQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.repository.MenuItemRepository;
import com.aurum.main.repository.SearchStrategy;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final SearchStrategy<MenuItemDTO, MenuItemQuery> searchStrategy;

    public PageResponse<MenuItemDTO> getMenuItems(MenuItemQuery query) {
        return searchStrategy.search(query);
    }
}
