package com.spring_bot.dashboard.dto;

import java.util.Set;

public record UserView(Long id, String username, String displayName, boolean enabled, Set<String> roles) {}
