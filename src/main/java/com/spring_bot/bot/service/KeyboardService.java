package com.spring_bot.bot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeyboardService {

    private final LocalizationService localizationService;

    public KeyboardService(LocalizationService localizationService) {
        this.localizationService = localizationService;
    }

    public ReplyKeyboard mainMenu(Long chatId) {

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(localizationService.getLocalizedMessage(chatId, "menu.about"));
        row1.add(localizationService.getLocalizedMessage(chatId, "menu.experts"));
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(localizationService.getLocalizedMessage(chatId, "menu.ask"));
        row2.add(localizationService.getLocalizedMessage(chatId, "menu.language"));
        keyboard.add(row2);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        keyboardMarkup.setSelective(true);

        return keyboardMarkup;
    }
}