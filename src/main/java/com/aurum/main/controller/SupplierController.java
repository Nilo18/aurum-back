package com.aurum.main.controller;

import com.aurum.main.dto.ProductSectionSupplierDTO;
import com.aurum.main.dto.SupplierDTO;
import com.aurum.main.dto.requests. CreateSupplierRequest;
import com.aurum.main.dto.requests.SupplierQuery;
import com.aurum.main.dto.requests.UpdateSupplierRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.model.Product;
import com.aurum.main.service.SupplierService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Data
@RequestMapping(path = "/api/supplier")
public class SupplierController {
    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<PageResponse<SupplierDTO>> getSuppliers(@ModelAttribute SupplierQuery query) {
        return ResponseEntity.ok(supplierService.getSuppliers(query));
    }

    @GetMapping(path = "/names")
    public ResponseEntity<List<ProductSectionSupplierDTO>> getSupplierNames() {
        return ResponseEntity.ok(supplierService.getSupplierNames());
    }

    @GetMapping(path = "/available-categories")
    public ResponseEntity<Set<Product.ProductCategory>> getAvailableProductCategories() {
        return ResponseEntity.ok(supplierService.getAvailableProductCategories());
    }

    @PostMapping
    public ResponseEntity<SupplierDTO> addSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        return ResponseEntity.ok(supplierService.addSupplier(request));
    }

    @PutMapping
    public ResponseEntity<SupplierDTO> updateSupplier(@Valid @RequestBody UpdateSupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(request));
    }

    @DeleteMapping(path = "/{publicId}")
    public ResponseEntity<GenericResponse> deleteSupplier(@PathVariable String publicId) {
        return ResponseEntity.ok(supplierService.deleteSupplier(publicId));
    }
}
