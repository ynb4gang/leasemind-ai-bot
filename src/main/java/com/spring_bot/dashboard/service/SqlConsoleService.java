package com.spring_bot.dashboard.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class SqlConsoleService {
    private static final Pattern SELECT_PATTERN = Pattern.compile("(?is)^\\s*(select|with)\\b");
    private final JdbcTemplate jdbcTemplate;

    public SqlConsoleService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> executeReadOnly(String sql, Integer limit) {
        String normalized = sql == null ? "" : sql.trim();
        if (!SELECT_PATTERN.matcher(normalized).find()) {
            throw new IllegalArgumentException("Only SELECT/CTE queries are allowed");
        }
        if (normalized.contains(";")) {
            throw new IllegalArgumentException("Semicolons are not allowed in SQL console");
        }
        int safeLimit = limit == null || limit < 1 ? 100 : Math.min(limit, 500);
        String finalSql = "select * from (" + normalized + ") q limit " + safeLimit;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(finalSql);
        List<String> columns = rows.isEmpty()
                ? extractColumns(finalSql)
                : new ArrayList<>(rows.getFirst().keySet());
        return Map.of(
                "columns", columns,
                "rows", rows,
                "rowCount", rows.size(),
                "limit", safeLimit
        );
    }

    public List<Map<String, Object>> tables() {
        return jdbcTemplate.queryForList("""
                select table_schema, table_name
                from information_schema.tables
                where table_type = 'BASE TABLE'
                  and table_schema not in ('pg_catalog', 'information_schema')
                order by table_schema, table_name
                """);
    }

    public List<Map<String, Object>> columns(String schema, String table) {
        return jdbcTemplate.queryForList("""
                select column_name, data_type, is_nullable
                from information_schema.columns
                where table_schema = ? and table_name = ?
                order by ordinal_position
                """, schema, table);
    }

    public Map<String, Object> preview(String schema, String table, Integer limit) {
        int safeLimit = limit == null || limit < 1 ? 50 : Math.min(limit, 200);
        String qualified = quote(schema) + "." + quote(table);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select * from " + qualified + " limit " + safeLimit);
        List<Map<String, Object>> cols = columns(schema, table);
        return Map.of("columns", cols, "rows", rows, "rowCount", rows.size(), "limit", safeLimit);
    }

    private List<String> extractColumns(String sql) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql + " limit 0");
        List<String> columns = new ArrayList<>();
        int count = rowSet.getMetaData().getColumnCount();
        for (int i = 1; i <= count; i++) {
            columns.add(rowSet.getMetaData().getColumnName(i));
        }
        return columns;
    }

    private String quote(String identifier) {
        if (identifier == null || !identifier.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid identifier");
        }
        return '"' + identifier + '"';
    }
}
