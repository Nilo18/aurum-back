package com.aurum.main.repository;

import com.aurum.main.dto.MenuItemDTO;
import com.aurum.main.dto.requests.MenuItemQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.utils.DynamicSqlBuilder;
import lombok.Data;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Data
public class MenuItemRepositoryCustomImpl implements SearchStrategy<MenuItemDTO, MenuItemQuery> {
    private final JdbcClient jdbcClient;

    @Override
    public PageResponse<MenuItemDTO> search(MenuItemQuery query) {
        DynamicSqlBuilder builder = new DynamicSqlBuilder(
                """
                SELECT
                    m.public_id,
                    m.name,
                    m.category,
                    m.cuisine_type AS type,
                    m.price_per_person
                FROM menu_items m
                WHERE 1 = 1
                """,
                "SELECT COUNT(*) FROM menu_items m WHERE 1 = 1"
        );

        builder
                .addCondition(
                        query.getCategory() != null,
                        " AND m.category = :category ",
                        "category", query.getCategory() != null ? query.getCategory().name() : null
                )
                .addCondition(
                        query.getType() != null,
                        " AND m.cuisine_type = :type ",
                        "type", query.getType() != null ? query.getType().name() : null
                )
                .addCondition(
                        query.getPriceFrom() != null,
                        " AND m.price_per_person >= :priceFrom ",
                        "priceFrom", query.getPriceFrom()
                )
                .addCondition(
                        query.getPriceTo() != null,
                        " AND m.price_per_person <= :priceTo ",
                        "priceTo", query.getPriceTo()
                );

        String finalSql = builder.getSql().toString();
        String finalCountSql = builder.getCountSql().toString();
        Map<String, Object> params = builder.getParams();

        String cleanSortBy = "m.id";
        if ("name".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "m.name";
        } else if ("category".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "m.category";
        } else if ("type".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "m.cuisine_type";
        } else if ("pricePerPerson".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "m.price_per_person";
        } else if ("publicId".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "m.public_id";
        }

        String cleanSortDir = "DESC".equalsIgnoreCase(query.getSortDirection()) ? "DESC" : "ASC";
        finalSql += " ORDER BY " + cleanSortBy + " " + cleanSortDir;
        if (!"m.id".equals(cleanSortBy)) {
            finalSql += ", m.id ASC";
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

        List<MenuItemDTO> content = jdbcClient.sql(finalSql)
                .params(params)
                .query(MenuItemDTO.class)
                .list();

        return new PageResponse<>(content, pageNumber, pageSize, totalElements);
    }
}
