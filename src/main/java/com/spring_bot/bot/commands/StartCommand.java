package com.spring_bot.bot.commands;

import com.spring_bot.bot.events.MessageEvent;
import com.spring_bot.bot.service.KeyboardService;
import com.spring_bot.bot.service.LocalizationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class StartCommand implements Command {
    private final ApplicationEventPublisher eventPublisher;
    private final LocalizationService localizationService;
    private final KeyboardService keyboardService;

    public StartCommand(ApplicationEventPublisher eventPublisher,
                        LocalizationService localizationService,
                        KeyboardService keyboardService
    ) {
        this.eventPublisher = eventPublisher;
        this.localizationService = localizationService;
        this.keyboardService = keyboardService;
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }
        Long chatId = update.getMessage().getChatId();
        String localizedMessage = localizationService.getLocalizedMessage(
                chatId,
                "menu.start"
        );
        return update.getMessage().getText().equals(localizedMessage);
    }

    @Override
    public void handle(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String localizedMessage = localizationService.getLocalizedMessage(
                    chatId,
                    "menu.welcome"
            );

            SendMessage message = SendMessage // Create a message object
                    .builder()
                    .chatId(chatId)
                    .text(localizedMessage)
                    .replyMarkup(keyboardService.mainMenu(chatId))
                    .build();
            eventPublisher.publishEvent(new MessageEvent(this, message));
        }
    }

    @Override
    public String getCommand() {
        return CommandName.START.getName();
    }
}