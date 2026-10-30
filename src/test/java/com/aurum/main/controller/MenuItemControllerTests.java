package com.aurum.main.controller;

import com.aurum.main.dto.*;
import com.aurum.main.dto.requests.*;
import com.aurum.main.dto.responses.*;
import com.aurum.main.exception.*;
import com.aurum.main.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MenuItemControllerTests extends ControllerTestSupport {

    @Test void listBindsPaginationAndReturnsPage() throws Exception {
        when(menuItemService.getMenuItems(any(MenuItemQuery.class))).thenReturn(new PageResponse<MenuItemDTO>(List.of(), 2, 5, 0));
        mvc.perform(get("/api/menu").header("Authorization", bearer("STAFF"))
                        .param("page", "2").param("size", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pageNumber").value(2))
                .andExpect(jsonPath("$.pageSize").value(5));
        verify(menuItemService).getMenuItems(argThat((MenuItemQuery q) -> q.getPage() == 2 && q.getSize() == 5));
    }

    @Test void ownerCanCreateAndReceivesResponse() throws Exception {
        when(menuItemService.addMenuItem(any(CreateMenuItemRequest.class))).thenReturn(new MenuItemDTO("item", "Cake", MenuItem.MenuItemCategory.DESSERT, MenuItem.CuisineType.GENERAL, BigDecimal.TEN));
        mvc.perform(post("/api/menu").header("Authorization", bearer("OWNER"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Cake","category":"DESSERT","type":"GENERAL","pricePerPerson":10}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.publicId").value("item"));
        verify(menuItemService).addMenuItem(any(CreateMenuItemRequest.class));
    }

    @Test void adminCanUpdate() throws Exception {
        when(menuItemService.updateMenuItem(any(UpdateMenuItemRequest.class))).thenReturn(new MenuItemDTO("item", "Cake", MenuItem.MenuItemCategory.DESSERT, MenuItem.CuisineType.GENERAL, BigDecimal.TEN));
        mvc.perform(put("/api/menu").header("Authorization", bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"publicId":"item","name":"Cake","category":"DESSERT","type":"GENERAL","pricePerPerson":10}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.publicId").value("item"));
    }

    @Test void invalidCreateAndUpdateAreRejectedBeforeService() throws Exception {
        String auth = bearer("OWNER");
        mvc.perform(post("/api/menu").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/menu").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        verifyNoInteractions(menuItemService);
    }

    @Test void staffCannotMutateResources() throws Exception {
        String auth = bearer("STAFF");
        mvc.perform(post("/api/menu").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Cake","category":"DESSERT","type":"GENERAL","pricePerPerson":10}
                """)).andExpect(status().isForbidden());
        mvc.perform(put("/api/menu").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"publicId":"item","name":"Cake","category":"DESSERT","type":"GENERAL","pricePerPerson":10}
                """)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/menu/item").header("Authorization", auth)).andExpect(status().isForbidden());
        verifyNoInteractions(menuItemService);
    }

    @Test void deletionReturnsSuccessAndMissingResourceReturns404() throws Exception {
        when(menuItemService.deleteMenuItem("item")).thenReturn(new GenericResponse(200, "Deleted"));
        when(menuItemService.deleteMenuItem("missing")).thenThrow(new MenuItemNotFoundException("Missing"));
        String auth = bearer("OWNER");
        mvc.perform(delete("/api/menu/item").header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Deleted"));
        mvc.perform(delete("/api/menu/missing").header("Authorization", auth)).andExpect(status().isNotFound());
    }

    @Test void eventMenuIsPublicAndDefaultsToGeneralCuisine() throws Exception {
        when(menuItemService.getMenuItems(MenuItem.CuisineType.GENERAL)).thenReturn(List.of());
        mvc.perform(get("/api/menu/request-event")).andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(menuItemService).getMenuItems(MenuItem.CuisineType.GENERAL);
        when(menuItemService.getMenuItems(MenuItem.CuisineType.GEORGIAN)).thenReturn(List.of());
        mvc.perform(get("/api/menu/request-event").param("cuisineType", "GEORGIAN")).andExpect(status().isOk());
        verify(menuItemService).getMenuItems(MenuItem.CuisineType.GEORGIAN);
    }

}
