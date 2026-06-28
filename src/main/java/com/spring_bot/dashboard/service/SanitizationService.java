package com.spring_bot.dashboard.service;

import org.springframework.stereotype.Service;

@Service
public class SanitizationService {
    public String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        return input
                .replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", "[email]")
                .replaceAll("(?i)bearer\\s+[A-Za-z0-9._-]+", "Bearer [token]")
                .replaceAll("(?i)(api[_-]?key|token|secret|password)\\s*[:=]\\s*[^\\s,;]+", "$1=[masked]")
                .replaceAll("\\+?\\d[\\d\\s()\\-]{7,}\\d", "[phone]");
    }
}
