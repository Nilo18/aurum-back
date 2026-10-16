package com.aurum.main.service;

import com.aurum.main.dto.requests.UpdateSupplierRequest;
import com.aurum.main.exception.SupplierNotFoundException;
import com.aurum.main.model.Product.ProductCategory;
import com.aurum.main.model.Supplier;
import com.aurum.main.repository.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierServiceTests {
    private final SupplierRepository suppliers = mock(SupplierRepository.class);
    private final SupplierCategoryRepository categories = mock(SupplierCategoryRepository.class);
    private final ProductRepository products = mock(ProductRepository.class);
    private final SupplierService service = new SupplierService(null, suppliers, categories, products);

    private UpdateSupplierRequest request() {
        return new UpdateSupplierRequest("public-id", "Updated", "City", "Street", "12",
                Set.of(ProductCategory.GLASSWARE, ProductCategory.FLORALS));
    }

    @Test
    void updateReplacesAssignmentsAndSynchronizesProducts() {
        Supplier supplier = new Supplier();
        supplier.setId(42L);
        supplier.setPublicId("public-id");
        when(suppliers.findByPublicId("public-id")).thenReturn(Optional.of(supplier));
        when(suppliers.save(supplier)).thenReturn(supplier);

        var result = service.updateSupplier(request());

        var order = inOrder(suppliers, categories, products);
        order.verify(suppliers).save(supplier);
        order.verify(products).unassignProductsOutsideCategories(42L, Set.of("GLASSWARE", "FLORALS"));
        order.verify(categories).deleteBySupplierId(42L);
        order.verify(categories).saveAll(42L, "public-id", request().categories());
        order.verify(products).assignUnassignedProducts(42L, Set.of("GLASSWARE", "FLORALS"));
        assertEquals("Updated", supplier.getName());
        assertEquals("City", supplier.getCity());
        assertEquals("Street", supplier.getStreet());
        assertEquals("12", supplier.getNumber());
        assertEquals("public-id", result.publicId());
        assertEquals(request().categories(), result.categories());
    }

    @Test
    void missingSupplierDoesNotChangeAssignmentsOrProducts() {
        when(suppliers.findByPublicId("public-id")).thenReturn(Optional.empty());

        assertThrows(SupplierNotFoundException.class, () -> service.updateSupplier(request()));

        verify(suppliers, never()).save(any());
        verifyNoInteractions(categories, products);
    }
}
