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

class SupplierControllerTests extends ControllerTestSupport {

    @Test void listBindsPaginationAndReturnsPage() throws Exception {
        when(supplierService.getSuppliers(any(SupplierQuery.class))).thenReturn(new PageResponse<SupplierDTO>(List.of(), 2, 5, 0));
        mvc.perform(get("/api/supplier").header("Authorization", bearer("STAFF"))
                        .param("page", "2").param("size", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pageNumber").value(2))
                .andExpect(jsonPath("$.pageSize").value(5));
        verify(supplierService).getSuppliers(argThat((SupplierQuery q) -> q.getPage() == 2 && q.getSize() == 5));
    }

    @Test void ownerCanCreateAndReceivesResponse() throws Exception {
        when(supplierService.addSupplier(any(CreateSupplierRequest.class))).thenReturn(new SupplierDTO("item", "Supplier", Set.of(Product.ProductCategory.TABLEWARE), "City", "Street", "12"));
        mvc.perform(post("/api/supplier").header("Authorization", bearer("OWNER"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Supplier","city":"City","street":"Street","number":"12","categories":["TABLEWARE"]}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.publicId").value("item"));
        verify(supplierService).addSupplier(any(CreateSupplierRequest.class));
    }

    @Test void adminCanUpdate() throws Exception {
        when(supplierService.updateSupplier(any(UpdateSupplierRequest.class))).thenReturn(new SupplierDTO("item", "Supplier", Set.of(Product.ProductCategory.TABLEWARE), "City", "Street", "12"));
        mvc.perform(put("/api/supplier").header("Authorization", bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"publicId":"item","name":"Supplier","city":"City","street":"Street","number":"12","categories":["TABLEWARE"]}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.publicId").value("item"));
    }

    @Test void invalidCreateAndUpdateAreRejectedBeforeService() throws Exception {
        String auth = bearer("OWNER");
        mvc.perform(post("/api/supplier").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/supplier").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        verifyNoInteractions(supplierService);
    }

    @Test void staffCannotMutateResources() throws Exception {
        String auth = bearer("STAFF");
        mvc.perform(post("/api/supplier").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Supplier","city":"City","street":"Street","number":"12","categories":["TABLEWARE"]}
                """)).andExpect(status().isForbidden());
        mvc.perform(put("/api/supplier").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"publicId":"item","name":"Supplier","city":"City","street":"Street","number":"12","categories":["TABLEWARE"]}
                """)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/supplier/item").header("Authorization", auth)).andExpect(status().isForbidden());
        verifyNoInteractions(supplierService);
    }

    @Test void deletionReturnsSuccessAndMissingResourceReturns404() throws Exception {
        when(supplierService.deleteSupplier("item")).thenReturn(new GenericResponse(200, "Deleted"));
        when(supplierService.deleteSupplier("missing")).thenThrow(new SupplierNotFoundException("Missing"));
        String auth = bearer("OWNER");
        mvc.perform(delete("/api/supplier/item").header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Deleted"));
        mvc.perform(delete("/api/supplier/missing").header("Authorization", auth)).andExpect(status().isNotFound());
    }

    @Test void anonymousListIsUnauthorized() throws Exception {
        mvc.perform(get("/api/supplier")).andExpect(status().isUnauthorized());
        verifyNoInteractions(supplierService);
    }

    @Test void supplierNamesAndAvailableCategoriesAreReturned() throws Exception {
        when(supplierService.getSupplierNames()).thenReturn(List.of(new ProductSectionSupplierDTO("supplier", "Name")));
        when(supplierService.getAvailableProductCategories()).thenReturn(Set.of(Product.ProductCategory.TABLEWARE));
        String auth = bearer("STAFF");
        mvc.perform(get("/api/supplier/names").header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].publicId").value("supplier"));
        mvc.perform(get("/api/supplier/available-categories").header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(content().json("[\"TABLEWARE\"]"));
    }

}
