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
    @SuppressWarnings("unchecked")
    private final SearchStrategy<com.aurum.main.dto.SupplierDTO, com.aurum.main.dto.requests.SupplierQuery> search = mock(SearchStrategy.class);
    private final SupplierService service = new SupplierService(search, suppliers, categories, products);

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

    @Test void creationAssignsOnlyUnassignedProductsInSelectedCategories() {
        when(suppliers.save(any(Supplier.class))).thenAnswer(call -> {
            Supplier supplier = call.getArgument(0); supplier.setId(42L); return supplier;
        });
        var request = new com.aurum.main.dto.requests.CreateSupplierRequest("Supplier", "City", "Street", "12", Set.of(ProductCategory.TABLEWARE));
        var result = service.addSupplier(request);
        assertDoesNotThrow(() -> java.util.UUID.fromString(result.publicId()));
        assertEquals(request.categories(), result.categories());
        verify(categories).saveAll(42L, result.publicId(), request.categories());
        verify(products).assignUnassignedProducts(42L, Set.of("TABLEWARE"));
        verify(products, never()).unassignProductsOutsideCategories(anyLong(), anySet());
    }

    @Test void availableCategoriesExcludeAssignedCategories() {
        when(categories.findAssignedCategories()).thenReturn(Set.of(ProductCategory.TABLEWARE));
        var expected = java.util.EnumSet.allOf(ProductCategory.class); expected.remove(ProductCategory.TABLEWARE);
        assertEquals(expected, service.getAvailableProductCategories());
    }

    @Test void searchAndSupplierNamesReturnRepositoryResults() {
        var query = new com.aurum.main.dto.requests.SupplierQuery();
        var page = new com.aurum.main.dto.responses.PageResponse<com.aurum.main.dto.SupplierDTO>(java.util.List.of(), 0, 10, 0);
        when(search.search(query)).thenReturn(page);
        assertSame(page, service.getSuppliers(query));
        var names = java.util.List.of(new com.aurum.main.dto.ProductSectionSupplierDTO("supplier", "Name"));
        when(suppliers.findAllSupplierNames()).thenReturn(names);
        assertSame(names, service.getSupplierNames());
    }

    @Test void deletionDistinguishesMissingAndExistingSupplier() {
        assertThrows(SupplierNotFoundException.class, () -> service.deleteSupplier("missing"));
        when(suppliers.deleteByPublicId("supplier")).thenReturn(1);
        assertEquals(new com.aurum.main.dto.responses.GenericResponse(200, "Supplier deleted successfully"), service.deleteSupplier("supplier"));
    }

}
