package com.spring_bot.dashboard.dto;

import java.util.Set;

public record CreateUserRequest(
        String username,
        String password,
        String displayName,
        Set<String> roles
) {}
