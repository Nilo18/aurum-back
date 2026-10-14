package com.aurum.main.service;

import com.aurum.main.dto.ProductDTO;
import com.aurum.main.dto.ProductRowDTO;
import com.aurum.main.dto.requests.CreateProductRequest;
import com.aurum.main.dto.requests.ProductQuery;
import com.aurum.main.dto.requests.UpdateProductRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.exception.ProductAlreadyExistsException;
import com.aurum.main.exception.ProductNotFoundException;
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
    private final SearchStrategy<ProductRowDTO, ProductQuery> searchStrategy;

    public PageResponse<ProductRowDTO> getProducts(ProductQuery query) {
        Long supplierId = query.getSupplierId();

        if (supplierId != null && !supplierRepository.existsById(supplierId)) {
            throw new SupplierNotFoundException("Supplier not found");
        }

        return searchStrategy.search(query);
    }

    public ProductDTO addProduct(CreateProductRequest request) {
        if (!supplierRepository.existsById(request.supplierId())) {
            throw new SupplierNotFoundException("Supplier not found");
        }

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

    public ProductDTO updateProduct(UpdateProductRequest request) {
        Product product = productRepository.findByPublicId(request.publicId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        product.setProductName(request.productName());
        product.setCategory(request.category());
        product.setSupplierId(request.supplierId());
        product.setQuantity(request.quantity());
        product.setPrice(request.price());
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

    public GenericResponse deleteProduct(String publicId) {
        int deleted = productRepository.deleteByPublicIdReturningInt(publicId);

        if (deleted == 0) {
            throw new ProductNotFoundException("Product not found");
        }

        return new GenericResponse(200, "Product deleted successfully");
    }
}