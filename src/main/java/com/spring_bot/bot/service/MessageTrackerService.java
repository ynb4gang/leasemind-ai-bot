package com.spring_bot.bot.service;

import com.spring_bot.bot.events.MessageEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MessageTrackerService {
    private final Map<Long, Integer> lastBotMessages = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;

    public MessageTrackerService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void saveLastMessage(Long chatId, Integer messageId) {
        lastBotMessages.put(chatId, messageId);
    }

    public void deleteLastMessage(Long chatId) {
        Integer lastMessageId = lastBotMessages.get(chatId);
        if (lastMessageId != null) {
            DeleteMessage deleteMessage = DeleteMessage.builder()
                    .chatId(chatId)
                    .messageId(lastMessageId)
                    .build();
            eventPublisher.publishEvent(new MessageEvent(this, deleteMessage));
        }
    }
}
