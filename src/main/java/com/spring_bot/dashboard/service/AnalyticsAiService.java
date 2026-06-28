package com.spring_bot.dashboard.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsAiService {

    private final ChatClient chatClient;
    private final AnalyticsPromptFactory promptFactory;

    public AnalyticsAiService(ChatClient.Builder chatClientBuilder,
                              AnalyticsPromptFactory promptFactory) {
        this.chatClient = chatClientBuilder.build();
        this.promptFactory = promptFactory;
    }

    public String generateOverviewInsights(String analyticsSnapshot, String sanitizedSamples) {
        return chatClient.prompt()
                .system(promptFactory.overviewSystemPrompt())
                .user(promptFactory.overviewUserPrompt(analyticsSnapshot, sanitizedSamples))
                .call()
                .content();
    }

    public String answerAnalyticsQuestion(String analyticsSnapshot,
                                          String sanitizedSamples,
                                          String analystQuestion) {
        return chatClient.prompt()
                .system(promptFactory.analyticsAssistantSystemPrompt())
                .user(promptFactory.analyticsAssistantUserPrompt(
                        analyticsSnapshot,
                        sanitizedSamples,
                        analystQuestion
                ))
                .call()
                .content();
    }

    public String generateQualitySummary(String analyticsSnapshot, String sanitizedSamples) {
        return chatClient.prompt()
                .system(promptFactory.qualitySystemPrompt())
                .user(promptFactory.qualityUserPrompt(analyticsSnapshot, sanitizedSamples))
                .call()
                .content();
    }
}