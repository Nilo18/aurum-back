package com.aurum.main.repository;

import com.aurum.main.dto.ClientDTO;
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
public class ClientRepositoryCustomImpl implements SearchStrategy<ClientDTO, ClientQuery>,
        MaskedSearchStrategy<ClientDTO, ClientQuery> {
    private final JdbcClient jdbcClient;

    @Override
    public PageResponse<ClientDTO> search(ClientQuery query) {
        return search(query, false);
    }

    @Override
    public PageResponse<ClientDTO> searchMasked(ClientQuery query) {
        return search(query, true);
    }

    private PageResponse<ClientDTO> search(ClientQuery query, boolean masked) {
        String contactColumns = masked ? """
                    CASE
                        WHEN c.email IS NULL THEN NULL
                        WHEN POSITION('@' IN c.email) <= 1
                            THEN REPEAT('*', LENGTH(c.email))
                        WHEN POSITION('@' IN c.email) = 2
                            THEN '*' || SUBSTRING(c.email FROM 2)
                        ELSE LEFT(c.email, 1)
                            || REPEAT('*', POSITION('@' IN c.email) - 2)
                            || SUBSTRING(c.email FROM POSITION('@' IN c.email))
                    END AS email,
                    CASE
                        WHEN LENGTH(REGEXP_REPLACE(c.phone, '[^0-9]', '', 'g')) <= 4
                            THEN REGEXP_REPLACE(c.phone, '[0-9]', '*', 'g')
                        ELSE REGEXP_REPLACE(
                            c.phone, '[0-9](?=(?:[^0-9]*[0-9]){4})', '*', 'g'
                        )
                    END AS phone
                """ : "c.email, c.phone";

        DynamicSqlBuilder builder = new DynamicSqlBuilder(
                """
                SELECT
                    c.type AS client_type,
                    c.name,
                    %s
                FROM client c
                WHERE 1 = 1
                """.formatted(contactColumns),
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
