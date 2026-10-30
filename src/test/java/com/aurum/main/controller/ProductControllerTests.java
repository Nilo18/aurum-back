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

class ProductControllerTests extends ControllerTestSupport {

    @Test void listBindsPaginationAndReturnsPage() throws Exception {
        when(productService.getProducts(any(ProductQuery.class))).thenReturn(new PageResponse<ProductRowDTO>(List.of(), 2, 5, 0));
        mvc.perform(get("/api/product").header("Authorization", bearer("STAFF"))
                        .param("page", "2").param("size", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pageNumber").value(2))
                .andExpect(jsonPath("$.pageSize").value(5));
        verify(productService).getProducts(argThat((ProductQuery q) -> q.getPage() == 2 && q.getSize() == 5));
    }

    @Test void ownerCanCreateAndReceivesResponse() throws Exception {
        when(productService.addProduct(any(CreateProductRequest.class))).thenReturn(new ProductDTO("item", "Rice", Product.ProductCategory.FOOD_INGREDIENT, BigDecimal.TEN, "supplier", 3));
        mvc.perform(post("/api/product").header("Authorization", bearer("OWNER"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"productName":"Rice","category":"FOOD_INGREDIENT","price":10,"supplierPublicId":"supplier","quantity":3}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.publicId").value("item"));
        verify(productService).addProduct(any(CreateProductRequest.class));
    }

    @Test void adminCanUpdate() throws Exception {
        when(productService.updateProduct(any(UpdateProductRequest.class))).thenReturn(new ProductDTO("item", "Rice", Product.ProductCategory.FOOD_INGREDIENT, BigDecimal.TEN, "supplier", 3));
        mvc.perform(put("/api/product").header("Authorization", bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"publicId":"item","productName":"Rice","category":"FOOD_INGREDIENT","price":10,"supplierPublicId":"supplier","quantity":3}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.publicId").value("item"));
    }

    @Test void invalidCreateAndUpdateAreRejectedBeforeService() throws Exception {
        String auth = bearer("OWNER");
        mvc.perform(post("/api/product").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/product").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        verifyNoInteractions(productService);
    }

    @Test void staffCannotMutateResources() throws Exception {
        String auth = bearer("STAFF");
        mvc.perform(post("/api/product").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"productName":"Rice","category":"FOOD_INGREDIENT","price":10,"supplierPublicId":"supplier","quantity":3}
                """)).andExpect(status().isForbidden());
        mvc.perform(put("/api/product").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"publicId":"item","productName":"Rice","category":"FOOD_INGREDIENT","price":10,"supplierPublicId":"supplier","quantity":3}
                """)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/product/item").header("Authorization", auth)).andExpect(status().isForbidden());
        verifyNoInteractions(productService);
    }

    @Test void deletionReturnsSuccessAndMissingResourceReturns404() throws Exception {
        when(productService.deleteProduct("item")).thenReturn(new GenericResponse(200, "Deleted"));
        when(productService.deleteProduct("missing")).thenThrow(new ProductNotFoundException("Missing"));
        String auth = bearer("OWNER");
        mvc.perform(delete("/api/product/item").header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Deleted"));
        mvc.perform(delete("/api/product/missing").header("Authorization", auth)).andExpect(status().isNotFound());
    }

    @Test void anonymousListIsUnauthorized() throws Exception {
        mvc.perform(get("/api/product")).andExpect(status().isUnauthorized());
        verifyNoInteractions(productService);
    }

}
