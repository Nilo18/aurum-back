package com.aurum.main.controller;

import com.aurum.main.dto.ProductSectionSupplierDTO;
import com.aurum.main.service.SupplierService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Data
@RequestMapping(path = "/api/supplier")
public class SupplierController {
    private final SupplierService supplierService;

    @GetMapping(path = "/names")
    public ResponseEntity<List<ProductSectionSupplierDTO>> getSupplierNames() {
        return ResponseEntity.ok(supplierService.getSupplierNames());
    }
}
