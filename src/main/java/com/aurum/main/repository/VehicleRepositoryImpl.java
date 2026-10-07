package com.aurum.main.repository;

import com.aurum.main.dto.VehicleDTO;
import com.aurum.main.dto.requests.VehicleQuery;
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
public class VehicleRepositoryImpl implements SearchStrategy<VehicleDTO, VehicleQuery> {
    private final JdbcClient jdbcClient;

    @Override
    public PageResponse<VehicleDTO> search(VehicleQuery query) {
        DynamicSqlBuilder builder = new DynamicSqlBuilder(
                """
                SELECT
                    v.type,
                    v.passenger_capacity,
                    v.cargo_weight_limit
                FROM vehicle v
                WHERE 1 = 1
                """,
                "SELECT COUNT(*) FROM vehicle v WHERE 1 = 1"
        );

        builder
                .addCondition(
                        StringUtils.hasText(query.getSearch()),
                        " AND LOWER(v.type) LIKE :search ",
                        "search", query.getSearch() != null ? "%" + query.getSearch().toLowerCase(Locale.ROOT) + "%" : null
                )
                .addCondition(
                        query.getType() != null,
                        " AND v.type = :type ",
                        "type", query.getType() != null ? query.getType().name() : null
                )
                .addCondition(
                        query.getPassengerFrom() != null,
                        " AND v.passenger_capacity >= :passengerFrom ",
                        "passengerFrom", query.getPassengerFrom()
                )
                .addCondition(
                        query.getPassengerTo() != null,
                        " AND v.passenger_capacity <= :passengerTo ",
                        "passengerTo", query.getPassengerTo()
                )
                .addCondition(
                        query.getWeightFrom() != null,
                        " AND v.cargo_weight_limit >= :weightFrom ",
                        "weightFrom", query.getWeightFrom()
                )
                .addCondition(
                        query.getWeightTo() != null,
                        " AND v.cargo_weight_limit <= :weightTo ",
                        "weightTo", query.getWeightTo()
                );

        String finalSql = String.valueOf(builder.getSql());
        String finalCountSql = String.valueOf(builder.getCountSql());
        Map<String, Object> params = builder.getParams();

        String cleanSortBy = "v.id";
        if ("type".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "v.type";
        } else if ("passengerCapacity".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "v.passenger_capacity";
        } else if ("cargoWeightLimit".equalsIgnoreCase(query.getSortBy())) {
            cleanSortBy = "v.cargo_weight_limit";
        }

        String cleanSortDir = "DESC".equalsIgnoreCase(query.getSortDirection()) ? "DESC" : "ASC";
        finalSql += " ORDER BY " + cleanSortBy + " " + cleanSortDir;
        if (!"v.id".equals(cleanSortBy)) {
            finalSql += ", v.id ASC";
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

        List<VehicleDTO> content = jdbcClient.sql(finalSql)
                .params(params)
                .query(VehicleDTO.class)
                .list();

        return new PageResponse<VehicleDTO>(content, pageNumber, pageSize, totalElements);
    }
}
