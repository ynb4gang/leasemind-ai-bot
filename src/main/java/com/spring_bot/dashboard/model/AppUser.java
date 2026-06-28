package com.spring_bot.dashboard.model;

import java.time.LocalDateTime;
import java.util.Set;

public record AppUser(
        Long id,
        String username,
        String passwordHash,
        String displayName,
        boolean enabled,
        LocalDateTime createdAt,
        Set<AppRole> roles
) {
    public boolean hasRole(AppRole role) {
        return roles != null && roles.contains(role);
    }
}
