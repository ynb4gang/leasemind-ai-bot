package com.spring_bot.bot.commands;

import com.spring_bot.bot.events.MessageEvent;
import com.spring_bot.bot.service.AiService;
import com.spring_bot.bot.service.LocalizationService;
import com.spring_bot.bot.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class FactCommand implements Command {
    private final LocalizationService localizationService;
    private final ApplicationEventPublisher eventPublisher;
    private final AiService aiService;
    private final UserSessionService userSessionService;

    public FactCommand(LocalizationService localization,
                       ApplicationEventPublisher eventPublisher,
                       AiService aiService,
                       UserSessionService userSessionService
    ) {
        this.localizationService = localization;
        this.eventPublisher = eventPublisher;
        this.aiService = aiService;
        this.userSessionService = userSessionService;
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }
        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        return messageText.equals(localizationService.getLocalizedMessage(chatId, "menu.fact"));
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        String fact = aiService.randomFact(userSessionService.getLocale(chatId));
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(fact)
                .build();
        eventPublisher.publishEvent(new MessageEvent(this, message));
    }

    @Override
    public String getCommand() {
        return CommandName.FACT.getName();
    }
}