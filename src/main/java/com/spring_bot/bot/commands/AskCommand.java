package com.spring_bot.bot.commands;

import com.spring_bot.bot.events.MessageEvent;
import com.spring_bot.bot.model.UserState;
import com.spring_bot.bot.service.LocalizationService;
import com.spring_bot.bot.service.UserSessionService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class AskCommand implements Command {

    private final UserSessionService userSessionService;
    private final LocalizationService localizationService;
    private final ApplicationEventPublisher eventPublisher;

    public AskCommand(
            UserSessionService userSessionService,
            LocalizationService localization,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userSessionService = userSessionService;
        this.localizationService = localization;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }
        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        return messageText.equals(localizationService.getLocalizedMessage(chatId, "menu.ask"));
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        // Устанавливаем состояние ожидания вопроса
        userSessionService.setUserState(chatId, UserState.WAITING_FOR_QUESTION);
        // Отправляем сообщение о готовности ответить на вопрос
        String prompt = localizationService.getLocalizedMessage(chatId, "ask.spring.ready");
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(prompt)
                .build();
        eventPublisher.publishEvent(new MessageEvent(this, message));
    }

    @Override
    public String getCommand() {
        return CommandName.ASK.getName();
    }
}
