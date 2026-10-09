package com.aurum.main.repository;

import com.aurum.main.dto.ProductDTO;
import com.aurum.main.dto.requests.ProductQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.utils.DynamicSqlBuilder;
import lombok.Data;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Repository
@Data
public class ProductRepositoryCustomImpl implements SearchStrategy<ProductDTO, ProductQuery> {
    private final JdbcClient jdbcClient;

    @Override
    public PageResponse<ProductDTO> search(ProductQuery query) {
        DynamicSqlBuilder builder = new DynamicSqlBuilder(
                """
                SELECT
                    p.public_id,
                    p.product_name,
                    p.category,
                    p.price,
                    p.supplier_id,
                    p.quantity
                FROM product p
                WHERE 1 = 1
                """,
                "SELECT COUNT(*) FROM product p WHERE 1 = 1"
        );

        builder
                .addCondition(
                        StringUtils.hasText(query.getSearch()),
                        " AND LOWER(p.product_name) LIKE :search ",
                        "search", query.getSearch() != null
                                ? "%" + query.getSearch().toLowerCase(Locale.ROOT) + "%" : null
                )
                .addCondition(
                        query.getProductCategory() != null,
                        " AND p.category = :category ",
                        "category", query.getProductCategory() != null ? query.getProductCategory().name() : null
                )
                .addCondition(
                        query.getPriceFrom() != null,
                        " AND p.price >= :priceFrom ",
                        "priceFrom", query.getPriceFrom()
                )
                .addCondition(
                        query.getPriceTo() != null,
                        " AND p.price <= :priceTo ",
                        "priceTo", query.getPriceTo()
                )
                .addCondition(
                        query.getSupplierId() != null,
                        " AND p.supplier_id = :supplierId ",
                        "supplierId", query.getSupplierId()
                )
                .addCondition(
                        query.getQuantityFrom() != null,
                        " AND p.quantity >= :quantityFrom ",
                        "quantityFrom", query.getQuantityFrom()
                )
                .addCondition(
                        query.getQuantityTo() != null,
                        " AND p.quantity <= :quantityTo ",
                        "quantityTo", query.getQuantityTo()
                );

        String finalSql = builder.getSql().toString();
        String finalCountSql = builder.getCountSql().toString();
        Map<String, Object> params = builder.getParams();

        String cleanSortBy = "p.id";
        if ("productName".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "p.product_name";
        } else if ("category".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "p.category";
        } else if ("price".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "p.price";
        } else if ("supplierId".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "p.supplier_id";
        } else if ("quantity".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "p.quantity";
        } else if ("publicId".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "p.public_id";
        }

        String cleanSortDir = "DESC".equalsIgnoreCase(query.getSortDirection()) ? "DESC" : "ASC";
        finalSql += " ORDER BY " + cleanSortBy + " " + cleanSortDir;
        if (!"p.id".equals(cleanSortBy)) {
            finalSql += ", p.id ASC";
        }

        int pageSize = (query.getSize() != null && query.getSize() > 0) ? query.getSize() : 10;
        int pageNumber = (query.getPage() != null && query.getPage() >= 0) ? query.getPage() : 0;
        long offset = (long) pageNumber * pageSize;

        finalSql += " LIMIT :limit OFFSET :offset ";
        params.put("limit", pageSize);
        params.put("offset", offset);

        long totalElements = jdbcClient.sql(finalCountSql)
                .params(params)
                .query(Long.class)
                .single();

        List<ProductDTO> content = jdbcClient.sql(finalSql)
                .params(params)
                .query(ProductDTO.class)
                .list();

        return new PageResponse<>(content, pageNumber, pageSize, totalElements);
    }
}
