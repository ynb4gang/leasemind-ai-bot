package com.spring_bot.dashboard.model;

import java.time.LocalDateTime;

public record QaMessage(
        Long id,
        String channel,
        Long chatId,
        String username,
        String questionText,
        String answerText,
        String sanitizedQuestion,
        String sanitizedAnswer,
        LocalDateTime createdAt
) {}
