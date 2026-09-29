package com.aurum.main.repository;

import com.aurum.main.dto.ClientDTO;
import com.aurum.main.dto.EventDTO;
import com.aurum.main.dto.requests.ClientQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.utils.DynamicSqlBuilder;
import lombok.Data;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Repository
@Data
public class ClientRepositoryCustomImpl implements ClientRepositoryCustom {
    private final JdbcClient jdbcClient;

    @Override
    public PageResponse<ClientDTO> searchClients(ClientQuery query) {
        DynamicSqlBuilder builder = new DynamicSqlBuilder(
                """
                SELECT
                    c.type AS client_type,
                    c.name,
                    c.email,
                    c.phone
                FROM client c
                WHERE 1 = 1
                """,
                "SELECT COUNT(*) FROM client c WHERE 1 = 1"
        );

// 2. Declarative filter assembly
        builder
                .addCondition(
                        StringUtils.hasText(query.getSearch()),
                        " AND (LOWER(c.name) LIKE :search " +
                                "OR LOWER(c.email) LIKE :search " +
                                "OR LOWER(c.phone) LIKE :search) ",
                        "search", query.getSearch() != null ? "%" + query.getSearch().toLowerCase() + "%" : null
                )
                .addCondition(
                        query.getType() != null,
                        " AND c.type = :type ",
                        "type", query.getType() != null ?
                                query.getType().name() : null
                );

        // 3. Extract your final queries and parameters
        String finalSql = String.valueOf(builder.getSql());
        String finalCountSql = String.valueOf(builder.getCountSql());
        Map<String, Object> params = builder.getParams();

        String cleanSortBy = "c.id";
        if ("type".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "c.type";
        } else if ("name".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "c.name";
        } else if ("email".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "c.email";
        } else {
            cleanSortBy = "c.phone";
        }

        String cleanSortDir = "DESC".equalsIgnoreCase(query.getSortDirection()) ? "DESC" : "ASC";
        finalSql += " ORDER BY " + cleanSortBy + " " + cleanSortDir;

        int pageSize = (query.getSize() != null && query.getSize() > 0) ? query.getSize() : 10;
        int pageNumber = (query.getPage() != null && query.getPage() >= 0) ? query.getPage() : 0;
        int offset = pageNumber * pageSize;

        finalSql += " LIMIT :limit OFFSET :offset ";
        params.put("limit", pageSize);
        params.put("offset", offset);

        long totalElements = jdbcClient.sql(finalCountSql)
                .params(params)
                .query(Long.class)
                .single();

        List<ClientDTO> content = jdbcClient.sql(finalSql)
                .params(params)
                .query(ClientDTO.class)
                .list();

        return new PageResponse<ClientDTO>(
                content,
                pageNumber,
                pageSize,
                totalElements
        );
    }
}
