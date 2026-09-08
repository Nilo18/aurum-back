package com.aurum.main.service;

import com.aurum.main.model.MenuItem;
import lombok.Data;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
public class MenuService {
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public List<MenuItem> getMenuItems(MenuItem.CuisineType cuisineType) {
        if (cuisineType == MenuItem.CuisineType.GENERAL) {
            return jdbcAggregateTemplate.findAll(MenuItem.class);
        }

        Criteria criteria = Criteria.where("cuisine_type")
                .in(cuisineType, MenuItem.CuisineType.GENERAL);

        Query query = Query.query(criteria);

        return jdbcAggregateTemplate.findAll(query, MenuItem.class);
    }
}
