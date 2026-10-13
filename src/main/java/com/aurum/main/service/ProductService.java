package com.aurum.main.service;

import com.aurum.main.dto.ProductDTO;
import com.aurum.main.dto.requests.CreateProductRequest;
import com.aurum.main.dto.requests.ProductQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.exception.ProductAlreadyExistsException;
import com.aurum.main.exception.SupplierNotFoundException;
import com.aurum.main.model.Product;
import com.aurum.main.model.Supplier;
import com.aurum.main.repository.ProductRepository;
import com.aurum.main.repository.SearchStrategy;
import com.aurum.main.repository.SupplierRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

    public ProductDTO addProduct(CreateProductRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found"));

        if (productRepository.existsByProductName(request.productName())) {
            throw new ProductAlreadyExistsException("Product already exists");
        }

        Product product = new Product();
        product.setProductName(request.productName());
        product.setCategory(request.category());
        product.setSupplierId(request.supplierId());
        product.setQuantity(request.quantity());
        product.setPrice(request.price());
        String publicId = UUID.randomUUID().toString();
        product.setPublicId(publicId);
        product = productRepository.save(product);

        return new ProductDTO(
                product.getPublicId(),
                product.getProductName(),
                product.getCategory(),
                product.getPrice(),
                product.getSupplierId(),
                product.getQuantity()
        );
    }
}