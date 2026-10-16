package com.aurum.main.repository;

import com.aurum.main.dto.SupplierCategoryDTO;
import com.aurum.main.model.Product;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Data
public class SupplierCategoryRepository {
    private final JdbcClient jdbcClient;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Set<Product.ProductCategory> findAssignedCategories() {
        return jdbcClient.sql("""
                SELECT category
                FROM supplier_categories
                ORDER BY category
                """)
                .query((rs, rowNum) ->
                        Product.ProductCategory.valueOf(rs.getString("category")))
                .set();
    }

    public Set<SupplierCategoryDTO> saveAll(Long supplierId, String supplierPublicId, Set<Product.ProductCategory> categories) {
        List<SqlParameterSource> batch = new ArrayList<>(categories.size());
        Set<SupplierCategoryDTO> result = new HashSet<>();

        for (Product.ProductCategory category : categories) {
            batch.add(new MapSqlParameterSource()
                    .addValue("supplierId", supplierId)
                    .addValue("category", category.name()));

            result.add(new SupplierCategoryDTO(supplierPublicId, category));
        }

        jdbcTemplate.batchUpdate("""
        INSERT INTO supplier_categories (supplier_id, category)
        VALUES (:supplierId, :category)
        """, batch.toArray(SqlParameterSource[]::new));

        return result;
    }
}
