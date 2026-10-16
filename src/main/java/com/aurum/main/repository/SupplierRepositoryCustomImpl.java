package com.aurum.main.repository;

import com.aurum.main.dto.SupplierDTO;
import com.aurum.main.dto.requests.SupplierQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.model.Product.ProductCategory;
import com.aurum.main.utils.DynamicSqlBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SupplierRepositoryCustomImpl implements SearchStrategy<SupplierDTO, SupplierQuery> {
    private final JdbcClient jdbcClient;

    @Override
    public PageResponse<SupplierDTO> search(SupplierQuery query) {
        DynamicSqlBuilder builder = new DynamicSqlBuilder(
                """
                SELECT s.public_id, s.name, s.city, s.street, s.number,
                    COALESCE((
                        SELECT string_agg(sc.category, ',' ORDER BY sc.category)
                        FROM supplier_categories sc
                        WHERE sc.supplier_id = s.id
                    ), '') AS categories
                FROM supplier s
                WHERE 1 = 1
                """,
                "SELECT COUNT(*) FROM supplier s WHERE 1 = 1"
        );

        builder.addCondition(
                StringUtils.hasText(query.getSearch()),
                " AND (LOWER(s.name) LIKE :search OR LOWER(s.city) LIKE :search " +
                        "OR LOWER(s.street) LIKE :search OR LOWER(s.number) LIKE :search) ",
                "search", query.getSearch() != null
                        ? "%" + query.getSearch().toLowerCase(Locale.ROOT) + "%" : null
        ).addCondition(
                query.getProductType() != null,
                " AND EXISTS (SELECT 1 FROM supplier_categories sc " +
                        "WHERE sc.supplier_id = s.id AND sc.category = :productType) ",
                "productType", query.getProductType() != null ? query.getProductType().name() : null
        );

        String sortBy = "s.id";
        if ("publicId".equalsIgnoreCase(query.getSortBy())) {
            sortBy = "s.public_id";
        } else if ("name".equalsIgnoreCase(query.getSortBy())) {
            sortBy = "s.name";
        } else if ("categories".equalsIgnoreCase(query.getSortBy())) {
            sortBy = "categories";
        } else if ("city".equalsIgnoreCase(query.getSortBy())) {
            sortBy = "s.city";
        } else if ("street".equalsIgnoreCase(query.getSortBy())) {
            sortBy = "s.street";
        } else if ("number".equalsIgnoreCase(query.getSortBy())) {
            sortBy = "s.number";
        }

        String sortDirection = "DESC".equalsIgnoreCase(query.getSortDirection()) ? "DESC" : "ASC";
        String sql = builder.getSql() + " ORDER BY " + sortBy + " " + sortDirection;
        if (!"s.id".equals(sortBy)) {
            sql += ", s.id ASC";
        }

        int pageSize = query.getSize() != null && query.getSize() > 0 ? query.getSize() : 10;
        int pageNumber = query.getPage() != null && query.getPage() >= 0 ? query.getPage() : 0;
        Map<String, Object> params = builder.getParams();
        params.put("limit", pageSize);
        params.put("offset", (long) pageNumber * pageSize);
        sql += " LIMIT :limit OFFSET :offset";

        long totalElements = jdbcClient.sql(builder.getCountSql().toString())
                .params(params)
                .query(Long.class)
                .single();

        List<SupplierDTO> content = jdbcClient.sql(sql)
                .params(params)
                .query((rs, rowNum) -> {
                    EnumSet<ProductCategory> categories = EnumSet.noneOf(ProductCategory.class);
                    String categoryNames = rs.getString("categories");
                    if (StringUtils.hasText(categoryNames)) {
                        for (String category : categoryNames.split(",")) {
                            categories.add(ProductCategory.valueOf(category));
                        }
                    }
                    return new SupplierDTO(
                            rs.getString("public_id"), rs.getString("name"), categories,
                            rs.getString("city"), rs.getString("street"), rs.getString("number")
                    );
                })
                .list();

        return new PageResponse<>(content, pageNumber, pageSize, totalElements);
    }
}
