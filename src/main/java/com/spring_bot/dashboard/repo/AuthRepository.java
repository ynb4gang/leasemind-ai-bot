package com.spring_bot.dashboard.repo;

import com.spring_bot.dashboard.model.AppRole;
import com.spring_bot.dashboard.model.AppUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class AuthRepository {
    private final JdbcTemplate jdbcTemplate;

    public AuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AppUser> baseMapper = (rs, rowNum) -> new AppUser(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("display_name"),
            rs.getBoolean("enabled"),
            Optional.ofNullable(rs.getTimestamp("created_at")).map(Timestamp::toLocalDateTime).orElse(null),
            Set.of()
    );

    public Optional<AppUser> findByUsername(String username) {
        List<AppUser> users = jdbcTemplate.query("""
                select id, username, password_hash, display_name, enabled, created_at
                from app_user
                where lower(username) = lower(?)
                """, baseMapper, username);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        AppUser user = users.getFirst();
        return Optional.of(withRoles(user));
    }

    public List<AppUser> findAllUsers() {
        List<AppUser> users = jdbcTemplate.query("""
                select id, username, password_hash, display_name, enabled, created_at
                from app_user
                order by id
                """, baseMapper);
        return users.stream().map(this::withRoles).toList();
    }

    private AppUser withRoles(AppUser user) {
        Set<AppRole> roles = new LinkedHashSet<>(jdbcTemplate.query("""
                select r.code
                from app_role r
                join app_user_role ur on ur.role_id = r.id
                where ur.user_id = ?
                order by r.code
                """, (rs, rowNum) -> AppRole.valueOf(rs.getString("code")), user.id()));
        return new AppUser(user.id(), user.username(), user.passwordHash(), user.displayName(), user.enabled(), user.createdAt(), roles);
    }

    public long createUser(String username, String passwordHash, String displayName, Set<AppRole> roles) {
        Number id = jdbcTemplate.queryForObject("""
                insert into app_user(username, password_hash, display_name, enabled, created_at)
                values (?, ?, ?, true, current_timestamp)
                returning id
                """, Number.class, username, passwordHash, displayName);
        if (id == null) {
            throw new IllegalStateException("Failed to create user");
        }
        Map<String, Long> roleIds = jdbcTemplate.query("select id, code from app_role", rs -> {
            Map<String, Long> out = new HashMap<>();
            while (rs.next()) {
                out.put(rs.getString("code"), rs.getLong("id"));
            }
            return out;
        });
        for (AppRole role : roles) {
            jdbcTemplate.update("insert into app_user_role(user_id, role_id) values (?, ?)", id.longValue(), roleIds.get(role.name()));
        }
        return id.longValue();
    }
}
