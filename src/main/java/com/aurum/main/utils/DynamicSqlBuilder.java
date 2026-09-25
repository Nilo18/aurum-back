package com.aurum.main.utils;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DynamicSqlBuilder {
    private final StringBuilder sql;
    private final StringBuilder countSql;
    private final Map<String, Object> params = new HashMap<>();

    public DynamicSqlBuilder(String baseSql, String baseCountSql) {
        this.sql = new StringBuilder(baseSql);
        this.countSql = new StringBuilder(baseCountSql);
    }

    public DynamicSqlBuilder addCondition(boolean condition, String sqlFragment, String paramKey, Object paramValue) {
        if (condition) {
            this.sql.append(sqlFragment);
            this.countSql.append(sqlFragment);
            this.params.put(paramKey, paramValue);
        }
        return this;
    }
}
