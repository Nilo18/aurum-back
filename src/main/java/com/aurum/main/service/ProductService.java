package com.aurum.main.service;

import com.aurum.main.dto.ProductDTO;
import com.aurum.main.dto.requests.ProductQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.exception.SupplierNotFoundException;
import com.aurum.main.repository.ProductRepository;
import com.aurum.main.repository.SearchStrategy;
import com.aurum.main.repository.SupplierRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class ProductService {
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final SearchStrategy<ProductDTO, ProductQuery> searchStrategy;

    public PageResponse<ProductDTO> getProducts(ProductQuery query) {
        Long supplierId = query.getSupplierId();

        if (supplierId != null && !supplierRepository.existsById(supplierId)) {
            throw new SupplierNotFoundException("Supplier not found");
        }

        return searchStrategy.search(query);
    }
}