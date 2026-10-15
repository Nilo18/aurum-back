package com.aurum.main.repository;

import com.aurum.main.dto.ProductSectionSupplierDTO;
import com.aurum.main.model.Supplier;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends CrudRepository<Supplier, Long> {
    Optional<Supplier> findByPublicId(String publicId);
    boolean existsByPublicId(String publicId);

    @Query("SELECT public_id, name FROM supplier ORDER BY name")
    List<ProductSectionSupplierDTO> findAllSupplierNames();
}
