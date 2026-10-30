package com.aurum.main.repository;

import com.aurum.main.dto.FeedbackDTO;
import com.aurum.main.dto.requests.FeedbackQuery;
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
public class FeedbackRepositoryCustomImpl implements SearchStrategy<FeedbackDTO, FeedbackQuery> {
    private final JdbcClient jdbcClient;

    @Override
    public PageResponse<FeedbackDTO> search(FeedbackQuery query) {
        DynamicSqlBuilder builder = new DynamicSqlBuilder(
                """
                SELECT f.public_id, f.author, f.comment_date, f.rating, f.description
                FROM feedback f
                WHERE 1 = 1
                """,
                "SELECT COUNT(*) FROM feedback f WHERE 1 = 1"
        );

        builder
                .addCondition(
                        StringUtils.hasText(query.getSearch()),
                        " AND (LOWER(f.author) LIKE :search OR LOWER(f.description) LIKE :search) ",
                        "search", query.getSearch() != null
                                ? "%" + query.getSearch().toLowerCase(Locale.ROOT) + "%" : null
                )
                .addCondition(query.getRatingFrom() != null,
                        " AND f.rating >= :ratingFrom ", "ratingFrom", query.getRatingFrom())
                .addCondition(query.getRatingTo() != null,
                        " AND f.rating <= :ratingTo ", "ratingTo", query.getRatingTo())
                .addCondition(query.getCommentFrom() != null,
                        " AND f.comment_date >= :commentFrom ", "commentFrom", query.getCommentFrom())
                .addCondition(query.getCommentTo() != null,
                        " AND f.comment_date <= :commentTo ", "commentTo", query.getCommentTo());

        String sortBy = "f.id";
        if ("author".equalsIgnoreCase(query.getSortBy())) {
            sortBy = "f.author";
        } else if ("commentDate".equalsIgnoreCase(query.getSortBy())) {
            sortBy = "f.comment_date";
        } else if ("rating".equalsIgnoreCase(query.getSortBy())) {
            sortBy = "f.rating";
        } else if ("description".equalsIgnoreCase(query.getSortBy())) {
            sortBy = "f.description";
        }
        String sortDirection = "DESC".equalsIgnoreCase(query.getSortDirection()) ? "DESC" : "ASC";
        String sql = builder.getSql() + " ORDER BY " + sortBy + " " + sortDirection;
        if (!"f.id".equals(sortBy)) {
            sql += ", f.id ASC";
        }

        int pageSize = query.getSize() != null && query.getSize() > 0 ? query.getSize() : 10;
        int pageNumber = query.getPage() != null && query.getPage() >= 0 ? query.getPage() : 0;
        sql += " LIMIT :limit OFFSET :offset";
        Map<String, Object> params = builder.getParams();
        params.put("limit", pageSize);
        params.put("offset", (long) pageNumber * pageSize);

        long totalElements = jdbcClient.sql(builder.getCountSql().toString())
                .params(params)
                .query(Long.class)
                .single();
        List<FeedbackDTO> content = jdbcClient.sql(sql)
                .params(params)
                .query(FeedbackDTO.class)
                .list();

        return new PageResponse<>(content, pageNumber, pageSize, totalElements);
    }
}
