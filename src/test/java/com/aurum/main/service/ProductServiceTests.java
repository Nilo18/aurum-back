package com.aurum.main.service;

import com.aurum.main.dto.ProductRowDTO;
import com.aurum.main.dto.requests.CreateProductRequest;
import com.aurum.main.dto.requests.ProductQuery;
import com.aurum.main.dto.requests.UpdateProductRequest;
import com.aurum.main.exception.SupplierNotFoundException;
import com.aurum.main.model.Product;
import com.aurum.main.model.Supplier;
import com.aurum.main.repository.ProductRepository;
import com.aurum.main.repository.SearchStrategy;
import com.aurum.main.repository.SupplierRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTests {
    private final ProductRepository products = mock(ProductRepository.class);
    private final SupplierRepository suppliers = mock(SupplierRepository.class);
    @SuppressWarnings("unchecked")
    private final SearchStrategy<ProductRowDTO, ProductQuery> search = mock(SearchStrategy.class);
    private final ProductService service = new ProductService(products, suppliers, search);

    private Supplier existingSupplier() {
        Supplier supplier = new Supplier();
        supplier.setId(42L);
        supplier.setPublicId("supplier-public-id");
        when(suppliers.findByPublicId(supplier.getPublicId())).thenReturn(Optional.of(supplier));
        when(products.save(any(Product.class))).thenAnswer(call -> call.getArgument(0));
        return supplier;
    }

    @Test
    void createResolvesPublicIdAndKeepsInternalForeignKey() {
        Supplier supplier = existingSupplier();
        var request = new CreateProductRequest("Rice", Product.ProductCategory.FOOD_INGREDIENT, BigDecimal.TEN, supplier.getPublicId(), 3);

        var response = service.addProduct(request);

        verify(products).save(argThat(product -> supplier.getId().equals(product.getSupplierId())));
        assertEquals(supplier.getPublicId(), response.supplierPublicId());
        assertNotNull(response.publicId());
    }

    @Test
    void updateResolvesPublicIdAndKeepsInternalForeignKey() {
        Supplier supplier = existingSupplier();
        Product product = new Product();
        product.setId(7L);
        product.setPublicId("product-public-id");
        when(products.findByPublicId(product.getPublicId())).thenReturn(Optional.of(product));
        var request = new UpdateProductRequest(product.getPublicId(), "Rice", Product.ProductCategory.FOOD_INGREDIENT,
                BigDecimal.TEN, supplier.getPublicId(), 3);

        var response = service.updateProduct(request);

        assertEquals(7L, product.getId());
        assertEquals(supplier.getId(), product.getSupplierId());
        assertEquals(supplier.getPublicId(), response.supplierPublicId());
    }

    @Test
    void missingSupplierCannotBeSavedOnUpdate() {
        when(products.findByPublicId("product-public-id")).thenReturn(Optional.of(new Product()));
        var request = new UpdateProductRequest("product-public-id", "Rice", Product.ProductCategory.FOOD_INGREDIENT,
                BigDecimal.TEN, "missing", 3);

        assertThrows(SupplierNotFoundException.class, () -> service.updateProduct(request));
        verify(products, never()).save(any());
    }

    @Test
    void filterChecksSupplierByPublicIdBeforeSearching() {
        ProductQuery query = new ProductQuery();
        query.setSupplierPublicId("supplier-public-id");
        when(suppliers.existsByPublicId(query.getSupplierPublicId())).thenReturn(true);

        service.getProducts(query);

        verify(search).search(query);
        verify(suppliers).existsByPublicId(query.getSupplierPublicId());
    }

    @Test void duplicateProductIsRejectedWithoutSaving() {
        var supplier = new Supplier(); supplier.setPublicId("supplier");
        when(suppliers.findByPublicId("supplier")).thenReturn(Optional.of(supplier));
        when(products.existsByProductName("Rice")).thenReturn(true);
        assertThrows(com.aurum.main.exception.ProductAlreadyExistsException.class, () -> service.addProduct(
                new CreateProductRequest("Rice", Product.ProductCategory.FOOD_INGREDIENT, BigDecimal.TEN, "supplier", 3)));
        verify(products, never()).save(any());
    }

    @Test void createRequiresExistingSupplier() {
        assertThrows(SupplierNotFoundException.class, () -> service.addProduct(
                new CreateProductRequest("Rice", Product.ProductCategory.FOOD_INGREDIENT, BigDecimal.TEN, "missing", 3)));
        verify(products, never()).save(any());
    }

    @Test void missingProductCannotBeUpdated() {
        assertThrows(com.aurum.main.exception.ProductNotFoundException.class, () -> service.updateProduct(
                new UpdateProductRequest("missing", "Rice", Product.ProductCategory.FOOD_INGREDIENT, BigDecimal.TEN, "supplier", 3)));
        verifyNoInteractions(suppliers);
        verify(products, never()).save(any());
    }

    @Test void nonexistentSupplierFilterDoesNotRunSearch() {
        var query = new ProductQuery(); query.setSupplierPublicId("missing");
        assertThrows(SupplierNotFoundException.class, () -> service.getProducts(query));
        verifyNoInteractions(search);
    }

    @Test void searchWithoutSupplierReturnsPage() {
        var query = new ProductQuery();
        var page = new com.aurum.main.dto.responses.PageResponse<ProductRowDTO>(java.util.List.of(), 0, 10, 0);
        when(search.search(query)).thenReturn(page);
        assertSame(page, service.getProducts(query));
        verifyNoInteractions(suppliers);
    }

    @Test void deletionDistinguishesMissingAndExistingProduct() {
        assertThrows(com.aurum.main.exception.ProductNotFoundException.class, () -> service.deleteProduct("missing"));
        when(products.deleteByPublicIdReturningInt("product")).thenReturn(1);
        assertEquals(new com.aurum.main.dto.responses.GenericResponse(200, "Product deleted successfully"), service.deleteProduct("product"));
    }

}
