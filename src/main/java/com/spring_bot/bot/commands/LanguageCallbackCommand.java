package com.spring_bot.bot.commands;

import com.spring_bot.bot.events.MessageEvent;
import com.spring_bot.bot.service.KeyboardService;
import com.spring_bot.bot.service.LocalizationService;
import com.spring_bot.bot.service.MessageTrackerService;
import com.spring_bot.bot.service.UserSessionService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class LanguageCallbackCommand implements Command {
    private final ApplicationEventPublisher eventPublisher;
    private final LocalizationService localizationService;
    private final UserSessionService userSessionService;
    private final KeyboardService keyboardService;
    private final MessageTrackerService messageTrackerService;

    public LanguageCallbackCommand(ApplicationEventPublisher eventPublisher,
                                   LocalizationService localizationService,
                                   UserSessionService userSessionService,
                                   KeyboardService keyboardService,
                                   MessageTrackerService messageTrackerService
    ) {
        this.eventPublisher = eventPublisher;
        this.localizationService = localizationService;
        this.userSessionService = userSessionService;
        this.keyboardService = keyboardService;
        this.messageTrackerService = messageTrackerService;
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasCallbackQuery()) {
            return false;
        }
        String callbackData = update.getCallbackQuery().getData();
        return callbackData.equals(LanguageCommand.LANG_RU)
                || callbackData.equals(LanguageCommand.LANG_EN);
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();
        messageTrackerService.deleteLastMessage(chatId);
        if (callbackData.equals(LanguageCommand.LANG_RU)) {
            userSessionService.setLocale(chatId, "ru");
            String switched = localizationService.getLocalizedMessage(
                    chatId,
                    "language.switched"
            );
            SendMessage message = SendMessage.builder()
                    .chatId(chatId.toString())
                    .replyMarkup(keyboardService.mainMenu(chatId))
                    .text(switched)
                    .build();
            eventPublisher.publishEvent(new MessageEvent(this, message));
        }
        else if (callbackData.equals(LanguageCommand.LANG_EN)) {
            userSessionService.setLocale(chatId, "en");
            String switched = localizationService.getLocalizedMessage(
                    chatId,
                    "language.switched"
            );
            SendMessage message = SendMessage.builder()
                    .chatId(chatId.toString())
                    .replyMarkup(keyboardService.mainMenu(chatId))
                    .text(switched)
                    .build();
            eventPublisher.publishEvent(new MessageEvent(this, message));
        }
    }

    @Override
    public String getCommand() {
        return CommandName.LANGUAGE.getName();
    }
}