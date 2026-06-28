package com.spring_bot.dashboard.dto;

import java.util.Set;

public record AuthView(String username, String displayName, Set<String> roles) {}
