package com.aurum.main.repository;

import com.aurum.main.dto.EventDTO;
import com.aurum.main.dto.requests.EventQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.model.Event;
import com.aurum.main.utils.DynamicSqlBuilder;
import lombok.Data;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Data
public class EventRepositoryCustomImpl implements EventRepositoryCustom {
    private final JdbcClient jdbcClient;

    @Override
    public PageResponse<EventDTO> searchEvents(EventQuery query) {
        DynamicSqlBuilder builder = new DynamicSqlBuilder(
                """
                SELECT\s
                    e.id,\s
                    e.event_type,\s
                    e.total_cost,\s
                    e.guest_count,
                    e.date,\s
                    e.location,\s
                    e.status,\s
                    c.name AS clientName\s
                FROM event e\s
                INNER JOIN client c ON e.client_id = c.id\s
                WHERE 1=1\s""",
                "SELECT COUNT(*) FROM event e INNER JOIN client c ON e.client_id = c.id WHERE 1=1 "
        );

// 2. Declarative filter assembly
        builder
                .addCondition(
                        StringUtils.hasText(query.getSearch()),
                        " AND LOWER(c.name) LIKE :search ",
                        "search", query.getSearch() != null ? "%" + query.getSearch().toLowerCase() + "%" : null
                )
                .addCondition(
                        query.getEventType() != null,
                        " AND event_type = :eventType ",
                        "eventType", query.getEventType() != null ?
                                query.getEventType().name() : null
                )
                .addCondition(
                        query.getCostFrom() != null,
                        " AND total_cost >= :costFrom ",
                        "costFrom", query.getCostFrom()
                )
                .addCondition(
                        query.getGuestFrom() != null,
                        " AND guest_count >= :guestFrom ",
                        "guestFrom", query.getGuestFrom()
                )
                .addCondition(
                        query.getGuestTo() != null,
                        " AND guest_count <= :guestTo",
                        "guestTo", query.getGuestTo()
                )
                .addCondition(
                        query.getCostTo() != null,
                        " AND total_cost <= :costTo ",
                        "costTo", query.getCostTo()
                )
                .addCondition(
                        query.getEventFrom() != null,
                        " AND date >= :eventFrom ",
                        "eventFrom", query.getEventFrom()
                )
                .addCondition(
                        query.getEventTo() != null,
                        " AND date <= :eventTo ", // Fixed to <= for upper bound range tracking
                        "eventTo", query.getEventTo()
                )
                .addCondition(
                        query.getLocation() != null,
                        " AND location = :location ",
                        "location", query.getLocation() != null ?
                                query.getLocation().name() : null
                )
                .addCondition(
                        query.getStatus() != null,
                        " AND status = :status ",
                        "status", query.getStatus() != null ?
                                query.getStatus().name() : null
                );

        // 3. Extract your final queries and parameters
        String finalSql = String.valueOf(builder.getSql());
        String finalCountSql = String.valueOf(builder.getCountSql());
        Map<String, Object> params = builder.getParams();

        String cleanSortBy = "e.id";
        if ("date".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "e.date";
        } else if ("cost".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "e.total_cost";
        } else {
            cleanSortBy = "c.name";
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

        List<EventDTO> content = jdbcClient.sql(finalSql)
                .params(params)
                .query(EventDTO.class)
                .list();

        return new PageResponse<EventDTO>(
            content,
            pageNumber,
            pageSize,
            totalElements
        );
    }
}
