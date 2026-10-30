package com.aurum.main.controller;

import com.aurum.main.dto.ProductDTO;
import com.aurum.main.dto.ProductRowDTO;
import com.aurum.main.dto.requests.CreateProductRequest;
import com.aurum.main.dto.requests.ProductQuery;
import com.aurum.main.dto.requests.UpdateProductRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.service.ProductService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/product")
@Data
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PageResponse<ProductRowDTO>> getProducts(@ModelAttribute ProductQuery query) {
        return ResponseEntity.ok(productService.getProducts(query));
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.addProduct(request));
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @PutMapping
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(request));
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<GenericResponse> deleteProduct(@PathVariable String publicId) {
        return ResponseEntity.ok(productService.deleteProduct(publicId));
    }
}
