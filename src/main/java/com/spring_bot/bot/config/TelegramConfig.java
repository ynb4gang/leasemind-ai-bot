package com.spring_bot.bot.config;

import com.spring_bot.bot.MainBot;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class TelegramConfig {
    @Bean
    public TelegramClient telegramClient(MainBot mainBot) {
        return new OkHttpTelegramClient(mainBot.getBotToken());
    }
}
