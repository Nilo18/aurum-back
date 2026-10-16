package com.aurum.main.service;

import com.aurum.main.dto.ProductSectionSupplierDTO;
import com.aurum.main.dto.SupplierDTO;
import com.aurum.main.dto.requests.SupplierQuery;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.repository.SearchStrategy;
import com.aurum.main.repository.SupplierRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
public class SupplierService {
    private final SearchStrategy<SupplierDTO, SupplierQuery> searchStrategy;
    private final SupplierRepository supplierRepository;

    public PageResponse<SupplierDTO> getSuppliers(SupplierQuery query) {
        return searchStrategy.search(query);
    }

    public List<ProductSectionSupplierDTO> getSupplierNames() {
        return supplierRepository.findAllSupplierNames();
    }

    public GenericResponse deleteSupplier() {
        return new GenericResponse(200, "Supplier deleted successfully");
    }
}
