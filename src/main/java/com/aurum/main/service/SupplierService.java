package com.aurum.main.service;

import com.aurum.main.dto.ProductSectionSupplierDTO;
import com.aurum.main.dto.SupplierCategoryDTO;
import com.aurum.main.dto.SupplierDTO;
import com.aurum.main.dto.requests.CreateSupplierRequest;
import com.aurum.main.dto.requests.SupplierQuery;
import com.aurum.main.dto.requests.UpdateSupplierRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.exception.BadRequestException;
import com.aurum.main.exception.SupplierNotFoundException;
import com.aurum.main.model.Product;
import com.aurum.main.model.Supplier;
import com.aurum.main.repository.ProductRepository;
import com.aurum.main.repository.SearchStrategy;
import com.aurum.main.repository.SupplierCategoryRepository;
import com.aurum.main.repository.SupplierRepository;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Data
public class SupplierService {
    private final SearchStrategy<SupplierDTO, SupplierQuery> searchStrategy;
    private final SupplierRepository supplierRepository;
    private final SupplierCategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public PageResponse<SupplierDTO> getSuppliers(SupplierQuery query) {
        return searchStrategy.search(query);
    }

    @Transactional
    public SupplierDTO addSupplier(CreateSupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setName(request.name());
        supplier.setCity(request.city());
        supplier.setStreet(request.street());
        supplier.setNumber(request.number());
        String publicId = UUID.randomUUID().toString();
        supplier.setPublicId(publicId);

        supplier = supplierRepository.save(supplier);

        categoryRepository.saveAll(supplier.getId(), supplier.getPublicId(), request.categories());

        productRepository.assignUnassignedProducts(
                supplier.getId(),
                request.categories().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet())
        );

        return new SupplierDTO(
                supplier.getPublicId(),
                supplier.getName(),
                request.categories(),
                supplier.getCity(),
                supplier.getStreet(),
                supplier.getNumber()
        );
    }

    @Transactional
    public SupplierDTO updateSupplier(UpdateSupplierRequest request) {
        Supplier supplier = supplierRepository.findByPublicId(request.publicId())
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found"));
        supplier.setName(request.name());
        supplier.setCity(request.city());
        supplier.setStreet(request.street());
        supplier.setNumber(request.number());
        supplier = supplierRepository.save(supplier);

        Set<String> categories = request.categories().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        productRepository.unassignProductsOutsideCategories(supplier.getId(), categories);

        categoryRepository.deleteBySupplierId(supplier.getId());

        categoryRepository.saveAll(supplier.getId(), supplier.getPublicId(), request.categories());
        productRepository.assignUnassignedProducts(supplier.getId(), categories);

        return new SupplierDTO(
                supplier.getPublicId(), supplier.getName(), request.categories(),
                supplier.getCity(), supplier.getStreet(), supplier.getNumber()
        );
    }

    public GenericResponse deleteSupplier(String publicId) {
        int deleted = supplierRepository.deleteByPublicId(publicId);

        if (deleted == 0) {
            throw new SupplierNotFoundException("Supplier not found");
        }

        return new GenericResponse(200, "Supplier deleted successfully");
    }

    public Set<Product.ProductCategory> getAvailableProductCategories() {
        Set<Product.ProductCategory> assigned =
                categoryRepository.findAssignedCategories();

        Set<Product.ProductCategory> available =
                EnumSet.allOf(Product.ProductCategory.class);

        assigned.forEach(available::remove);

        return available;
    }

    public List<ProductSectionSupplierDTO> getSupplierNames() {
        return supplierRepository.findAllSupplierNames();
    }
}