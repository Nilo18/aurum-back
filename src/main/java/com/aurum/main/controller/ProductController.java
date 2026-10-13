package com.aurum.main.controller;

import com.aurum.main.dto.ProductDTO;
import com.aurum.main.dto.requests.CreateProductRequest;
import com.aurum.main.dto.requests.ProductQuery;
import com.aurum.main.dto.requests.UpdateProductRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.service.ProductService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/product")
@Data
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PageResponse<ProductDTO>> getProducts(@ModelAttribute ProductQuery query) {
        return ResponseEntity.ok(productService.getProducts(query));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> addProduct(@RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.addProduct(request));
    }

    @PutMapping
    public ResponseEntity<ProductDTO> updateProduct(@RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }
}
