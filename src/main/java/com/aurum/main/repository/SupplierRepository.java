package com.aurum.main.repository;

import com.aurum.main.dto.ProductSectionSupplierDTO;
import com.aurum.main.model.Supplier;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends CrudRepository<Supplier, Long> {
    Optional<Supplier> findByPublicId(String publicId);
    boolean existsByPublicId(String publicId);

    @Query("SELECT public_id, name FROM supplier ORDER BY name")
    List<ProductSectionSupplierDTO> findAllSupplierNames();

    @Modifying
    @Query("DELETE FROM supplier WHERE public_id = :publicId")
    int deleteByPublicId(@Param("publicId") String publicId);
}
