package com.spring_bot.bot.commands;

import com.spring_bot.bot.events.MessageEvent;
import com.spring_bot.bot.model.UserState;
import com.spring_bot.bot.service.AiService;
import com.spring_bot.bot.service.UserSessionService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;

@Service
public class QuestionHandlerCommand implements Command {
    private final UserSessionService userSessionService;
    private final ApplicationEventPublisher eventPublisher;
    private final AiService aiService;

    public QuestionHandlerCommand(
            UserSessionService userSessionService,
            ApplicationEventPublisher eventPublisher,
            AiService aiService
    ) {
        this.userSessionService = userSessionService;
        this.eventPublisher = eventPublisher;
        this.aiService = aiService;
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }
        Long chatId = update.getMessage().getChatId();
        UserState userState = userSessionService.getUserState(chatId);
        return userState.equals(UserState.WAITING_FOR_QUESTION);
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.getMessage().getChatId();
        String question = update.getMessage().getText();
        Locale locale = userSessionService.getLocale(chatId);
        String answer = aiService.answerQuestion(question, locale);
        userSessionService.setUserState(chatId, UserState.IDLE);
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(answer)
                .build();
        eventPublisher.publishEvent(new MessageEvent(this, message));
    }

    @Override
    public String getCommand() {
        return CommandName.QUESTION_HANDLER.getName();
    }
}
