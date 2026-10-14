package com.aurum.main.service;

import com.aurum.main.dto.ProductSectionSupplierDTO;
import com.aurum.main.repository.SupplierRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public List<ProductSectionSupplierDTO> getSupplierNames() {
        return supplierRepository.findAllSupplierNames();
    }
}
