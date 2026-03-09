package com.spring_bot.bot.commands;

import com.spring_bot.bot.events.MessageEvent;
import com.spring_bot.bot.service.LocalizationService;
import com.spring_bot.bot.service.UserSessionService;
import com.spring_bot.bot.service.expert.Expert;
import com.spring_bot.bot.service.expert.ExpertService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExpertsCommand implements Command {

    public static final String EXPERT_PREFIX = "expert_";

    private final LocalizationService localizationService;
    private final ExpertService expertService;
    private final UserSessionService userSessionService;
    private final ApplicationEventPublisher eventPublisher;

    public ExpertsCommand(
            LocalizationService localizationService,
            ExpertService expertService,
            UserSessionService userSessionService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.localizationService = localizationService;
        this.expertService = expertService;
        this.userSessionService = userSessionService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }
        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        return messageText.equals(localizationService.getLocalizedMessage(chatId, "menu.experts"));
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        var experts = expertService.listExperts(userSessionService.getLocale(chatId));
        List<String> expertNames = experts.stream().map(Expert::getFullName).toList();
        List<String> expertIds = experts.stream().map(Expert::getId).toList();
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(localizationService.getLocalizedMessage(chatId, "ask.expert.select"))
                .replyMarkup(expertsInline(expertNames, expertIds))
                .build();
        eventPublisher.publishEvent(new MessageEvent(this, message));
    }

    @Override
    public String getCommand() {
        return CommandName.EXPERTS.getName();
    }

    private InlineKeyboardMarkup expertsInline(
            List<String> expertNames,
            List<String> expertIds
    ) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < expertNames.size(); i++) {
            rows.add(new InlineKeyboardRow(InlineKeyboardButton.builder()
                    .text(expertNames.get(i))
                    .callbackData(EXPERT_PREFIX + expertIds.get(i))
                    .build())
            );
        }
        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }
}
