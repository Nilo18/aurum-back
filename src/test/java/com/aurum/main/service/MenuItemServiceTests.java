package com.aurum.main.service;

import com.aurum.main.dto.*;
import com.aurum.main.dto.requests.*;
import com.aurum.main.dto.responses.*;
import com.aurum.main.exception.*;
import com.aurum.main.model.*;
import com.aurum.main.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTests {

    @Mock MenuItemRepository repository;
    @Mock SearchStrategy<MenuItemDTO, MenuItemQuery> search;
    @Mock org.springframework.data.jdbc.core.JdbcAggregateTemplate template;
    @InjectMocks MenuItemService service;

    @Test void generalCuisineReturnsAllMenuItems() {
        var rows = List.of(new MenuItem());
        when(template.findAll(MenuItem.class)).thenReturn(rows);
        assertSame(rows, service.getMenuItems(MenuItem.CuisineType.GENERAL));
        verify(template, never()).findAll(any(org.springframework.data.relational.core.query.Query.class), eq(MenuItem.class));
    }

    @Test void specificCuisineIncludesGeneralCuisine() {
        var rows = List.of(new MenuItem());
        when(template.findAll(any(org.springframework.data.relational.core.query.Query.class), eq(MenuItem.class))).thenReturn(rows);
        assertSame(rows, service.getMenuItems(MenuItem.CuisineType.GEORGIAN));
        var captor = org.mockito.ArgumentCaptor.forClass(org.springframework.data.relational.core.query.Query.class);
        verify(template).findAll(captor.capture(), eq(MenuItem.class));
        String criteria = captor.getValue().getCriteria().orElseThrow().toString();
        assertTrue(criteria.contains("cuisine_type"));
        assertTrue(criteria.contains("GEORGIAN"));
        assertTrue(criteria.contains("GENERAL"));
    }

    @Test void searchReturnsRequestedPage() {
        var query = new MenuItemQuery();
        var page = new PageResponse<MenuItemDTO>(List.of(), 0, 10, 0);
        when(search.search(query)).thenReturn(page);
        assertSame(page, service.getMenuItems(query));
    }

    @Test void creationPersistsFieldsAndReturnsGeneratedPublicId() {
        var request = new CreateMenuItemRequest("Cake", MenuItem.MenuItemCategory.DESSERT,
                MenuItem.CuisineType.GENERAL, BigDecimal.TEN);
        var result = service.addMenuItem(request);
        assertDoesNotThrow(() -> UUID.fromString(result.publicId()));
        assertEquals(new MenuItemDTO(result.publicId(), request.name(), request.category(), request.type(), request.pricePerPerson()), result);
        verify(repository).save(argThat(item -> item.getPublicId().equals(result.publicId())
                && item.getName().equals("Cake") && item.getPricePerPerson().equals(BigDecimal.TEN)));
    }

    @Test void updatePreservesIdentityAndChangesEditableFields() {
        var existing = new MenuItem();
        existing.setId(42L);
        existing.setPublicId("item");
        when(repository.findByPublicId("item")).thenReturn(Optional.of(existing));
        var request = new UpdateMenuItemRequest("item", "Cake", MenuItem.MenuItemCategory.DESSERT,
                MenuItem.CuisineType.GENERAL, BigDecimal.TEN);
        assertEquals(new MenuItemDTO("item", "Cake", request.category(), request.type(), BigDecimal.TEN),
                service.updateMenuItem(request));
        assertEquals(42L, existing.getId());
        verify(repository).save(existing);
    }

    @Test void updateMissingItemDoesNotSave() {
        assertThrows(MenuItemNotFoundException.class, () -> service.updateMenuItem(new UpdateMenuItemRequest(
                "missing", "Cake", MenuItem.MenuItemCategory.DESSERT, MenuItem.CuisineType.GENERAL, BigDecimal.TEN)));
        verify(repository, never()).save(any());
    }

    @Test void deletionDistinguishesMissingAndExistingItems() {
        assertThrows(MenuItemNotFoundException.class, () -> service.deleteMenuItem("missing"));
        when(repository.deleteByPublicId("item")).thenReturn(1);
        assertEquals(new GenericResponse(200, "Menu item deleted successfully"), service.deleteMenuItem("item"));
    }

}
