package com.aurum.main.repository;

import com.aurum.main.dto.EmployeeDTO;
import com.aurum.main.dto.requests.EmployeeQuery;
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
public class EmployeeRepositoryCustomImpl implements SearchStrategy<EmployeeDTO, EmployeeQuery> {
    private final JdbcClient jdbcClient;

    @Override
    public PageResponse<EmployeeDTO> search(EmployeeQuery query) {
        DynamicSqlBuilder builder = new DynamicSqlBuilder(
                """
                SELECT
                    e.type AS specialty,
                    e.name,
                    e.salary,
                    e.email,
                    e.role,
                    e.status
                FROM employees e
                WHERE 1 = 1
                """,
                "SELECT COUNT(*) FROM employees e WHERE 1 = 1"
        );

        builder
                .addCondition(
                        StringUtils.hasText(query.getSearch()),
                        " AND (LOWER(e.name) LIKE :search OR LOWER(e.email) LIKE :search) ",
                        "search", query.getSearch() != null ? "%" + query.getSearch().toLowerCase(Locale.ROOT) + "%" : null
                )
                .addCondition(
                        query.getType() != null,
                        " AND e.type = :type ",
                        "type", query.getType() != null ?
                                query.getType().name() : null
                )
                .addCondition(
                        query.getRole() != null,
                        " AND e.role = :role ",
                        "role", query.getRole() != null ? query.getRole().name() : null
                )
                .addCondition(
                        query.getStatus() != null,
                        " AND e.status = :status ",
                        "status", query.getStatus() != null ? query.getStatus().name() : null
                );

        String finalSql = String.valueOf(builder.getSql());
        String finalCountSql = String.valueOf(builder.getCountSql());
        Map<String, Object> params = builder.getParams();

        String cleanSortBy = "e.id";
        if ("name".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "e.name";
        } else if ("email".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "e.email";
        } else if ("salary".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "e.salary";
        }

        String cleanSortDir = "DESC".equalsIgnoreCase(query.getSortDirection()) ? "DESC" : "ASC";
        finalSql += " ORDER BY " + cleanSortBy + " " + cleanSortDir;
        if (!"e.id".equals(cleanSortBy)) {
            finalSql += ", e.id ASC";
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

        List<EmployeeDTO> content = jdbcClient.sql(finalSql)
                .params(params)
                .query(EmployeeDTO.class)
                .list();

        return new PageResponse<EmployeeDTO>(
                content,
                pageNumber,
                pageSize,
                totalElements
        );
    }
}
