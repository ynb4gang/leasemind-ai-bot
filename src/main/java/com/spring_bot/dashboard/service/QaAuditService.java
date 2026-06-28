package com.spring_bot.dashboard.service;

import com.spring_bot.dashboard.repo.QaMessageRepository;
import org.springframework.stereotype.Service;

@Service
public class QaAuditService {
    private final QaMessageRepository qaMessageRepository;
    private final SanitizationService sanitizationService;

    public QaAuditService(QaMessageRepository qaMessageRepository, SanitizationService sanitizationService) {
        this.qaMessageRepository = qaMessageRepository;
        this.sanitizationService = sanitizationService;
    }

    public void saveExchange(String channel, Long chatId, String username, String question, String answer) {
        qaMessageRepository.save(
                channel,
                chatId,
                username,
                question,
                answer,
                sanitizationService.sanitize(question),
                sanitizationService.sanitize(answer)
        );
    }
}
