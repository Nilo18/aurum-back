package com.aurum.main.repository;

import com.aurum.main.model.Product;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ProductRepository extends CrudRepository<Product, Long> {
    boolean existsByProductName(String productName);
    @Transactional
    @Modifying
    @Query("DELETE FROM product p WHERE p.id = :id")
    int deleteByIdReturningInt(@Param("id") Long id);
    @Modifying
    @Query("DELETE FROM product p WHERE p.public_id = :publicId")
    int deleteByPublicIdReturningInt(@Param("publicId") String publicId);
    Optional<Product> findByPublicId(String publicId);
}
