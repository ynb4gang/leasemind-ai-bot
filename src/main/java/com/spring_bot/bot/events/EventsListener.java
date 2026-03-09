package com.spring_bot.bot.events;

import com.spring_bot.bot.service.MessageTrackerService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;

@Component
public class EventsListener {

    private final TelegramClient telegramClient;
    private final MessageTrackerService messageTrackerService;

    public EventsListener(
            TelegramClient telegramClient,
            MessageTrackerService messageTrackerService
    ) {
        this.telegramClient = telegramClient;
        this.messageTrackerService = messageTrackerService;
    }

    @EventListener
    public void on(MessageEvent event) throws TelegramApiException {
        BotApiMethod<? extends Serializable> message = event.getMessage();
        Object sentMessage = telegramClient.execute(message);
        if (sentMessage instanceof Message &&
                ((Message) sentMessage).getMessageId() != null) {
            messageTrackerService.saveLastMessage(
                    ((Message) sentMessage).getChatId(),
                    ((Message) sentMessage).getMessageId()
            );
        }
    }

    @EventListener
    public void on(MemberEvent event) throws TelegramApiException {
        ChatMember executed = telegramClient.execute(GetChatMember.builder()
                .chatId(event.getChannelId())
                .userId(event.getChatId())
                .build()
        );
        String status = executed.getStatus();
        boolean isMember = status != null
                && !status.equalsIgnoreCase("left")
                && !status.equalsIgnoreCase("kicked");
        event.complete(isMember);
    }
}
