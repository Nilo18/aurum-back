package com.aurum.main.controller;

import com.aurum.main.dto.ProductDTO;
import com.aurum.main.dto.requests.ProductQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.service.ProductService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/product")
@Data
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PageResponse<ProductDTO>> getProducts(@ModelAttribute ProductQuery query) {
        return ResponseEntity.ok(productService.getProducts(query));
    }
}
