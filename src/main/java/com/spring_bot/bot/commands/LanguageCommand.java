package com.spring_bot.bot.commands;

import com.spring_bot.bot.events.MessageEvent;
import com.spring_bot.bot.service.LocalizationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class LanguageCommand implements Command {
    public static final String LANG_RU = "lang_ru";
    public static final String LANG_EN = "lang_en";

    private final ApplicationEventPublisher eventPublisher;
    private final LocalizationService localizationService;

    public LanguageCommand(ApplicationEventPublisher eventPublisher,
                           LocalizationService localizationService
    ) {
        this.eventPublisher = eventPublisher;
        this.localizationService = localizationService;
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }
        Long chatId = update.getMessage().getChatId();
        String localizedMessage = localizationService.getLocalizedMessage(
                chatId,
                "menu.language"
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
                    "language.select"
            );

            SendMessage message = SendMessage // Create a message object
                    .builder()
                    .chatId(chatId)
                    .text(localizedMessage)
                    .replyMarkup(languageInline(chatId))
                    .build();
            eventPublisher.publishEvent(new MessageEvent(this, message));
        }
    }

    private ReplyKeyboard languageInline(Long chatId) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text(localizationService.getLocalizedMessage(chatId, "language.ru"))
                .callbackData(LANG_RU)
                .build()));
        rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text(localizationService.getLocalizedMessage(chatId, "language.en"))
                .callbackData(LANG_EN)
                .build()));
        return InlineKeyboardMarkup
                .builder()
                .keyboard(rows)
                .build();
    }

    @Override
    public String getCommand() {
        return CommandName.LANGUAGE.getName();
    }
}
