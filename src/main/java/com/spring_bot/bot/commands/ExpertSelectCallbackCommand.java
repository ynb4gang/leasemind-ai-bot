package com.spring_bot.bot.commands;

import com.spring_bot.bot.events.MessageEvent;
import com.spring_bot.bot.model.UserState;
import com.spring_bot.bot.service.LocalizationService;
import com.spring_bot.bot.service.MessageTrackerService;
import com.spring_bot.bot.service.UserSessionService;
import com.spring_bot.bot.service.expert.ExpertService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ExpertSelectCallbackCommand implements Command {
    private final UserSessionService userSessionService;
    private final LocalizationService localizationService;
    private final ExpertService expertService;
    private final MessageTrackerService messageTrackerService;
    private final ApplicationEventPublisher	eventPublisher;

    public ExpertSelectCallbackCommand(
            UserSessionService userSessionService,
            LocalizationService localizationService,
            ExpertService expertService,
            MessageTrackerService messageTrackerService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userSessionService = userSessionService;
        this.localizationService = localizationService;
        this.expertService = expertService;
        this.messageTrackerService = messageTrackerService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasCallbackQuery()) {
            return false;
        }
        String callbackData = update.getCallbackQuery().getData();
        return callbackData.startsWith(ExpertsCommand.EXPERT_PREFIX);
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        // Удаляем предыдущее сообщение бота
        messageTrackerService.deleteLastMessage(chatId);
        String callbackData = update.getCallbackQuery().getData();
        String expertId = callbackData.substring(ExpertsCommand.EXPERT_PREFIX.length());
        userSessionService.setSelectedExpertId(chatId, expertId);
        userSessionService.setUserState(chatId, UserState.WAITING_FOR_EXPERT_QUESTION);
        var expert = expertService.getExpertById(
                expertId,
                userSessionService.getLocale(chatId)
        );
        String selectedExpertText = localizationService.getLocalizedMessage(
                chatId,
                "ask.expert.selected",
                expert
        );
        userSessionService.getLocale(chatId);
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(selectedExpertText)
                .build();
        eventPublisher.publishEvent(new MessageEvent(this, message));
    }

    @Override
    public String getCommand() {
        return CommandName.EXPERTS.getName() + "_SELECT_CALLBACK";
    }
}
