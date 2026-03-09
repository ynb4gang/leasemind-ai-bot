package com.spring_bot.bot.service;

import com.spring_bot.bot.service.expert.Expert;
import com.spring_bot.bot.service.expert.ExpertService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AiService {
    private final ChatClient chatClient;
    private final LocalizationService localizationService;
    private final ExpertService expertService;

    public AiService(
            ChatClient.Builder chatClientBuilder,
            LocalizationService localizationService,
            ExpertService expertService
    ) {
        this.chatClient = chatClientBuilder.build();
        this.localizationService = localizationService;
        this.expertService = expertService;
    }

    public String randomFact(Locale locale) {
        String localizedMessage = localizationService.getLocalizedMessage("ai.random.spring.fact",
                locale
        );
        return chatClient.prompt(localizedMessage).call().content();
    }

    public String answerQuestion(
            String question,
            Locale locale
    ) {
        String systemPrompt = localizationService.getLocalizedMessage(
                "ai.system.prompt",
                locale
        );
        return chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .call()
                .content();
    }

    public String answerAsExpert(String expertId, String question, Locale locale) {
        Expert expertById = expertService.getExpertById(expertId, locale);
        String expertPrompt = expertById.getPersonaPrompt();
        return chatClient.prompt()
                .system(expertPrompt)
                .user(question)
                .call()
                .content();
    }
}
