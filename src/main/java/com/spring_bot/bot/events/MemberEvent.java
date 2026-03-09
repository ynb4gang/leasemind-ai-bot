package com.spring_bot.bot.events;

import org.springframework.context.ApplicationEvent;

import java.util.concurrent.CompletableFuture;

public class MemberEvent extends ApplicationEvent {
    private final String channelId;
    private final Long chatId;
    private final CompletableFuture<Boolean> isMember;

    public MemberEvent(Object source,
                       String channelId,
                       Long chatId
    ) {
        super(source);

        this.channelId = channelId;
        this.chatId = chatId;
        this.isMember = new CompletableFuture<>();
    }

    public String getChannelId() {
        return channelId;
    }

    public Long getChatId() {
        return chatId;
    }

    public CompletableFuture<Boolean> getIsMember() {
        return isMember;
    }

    public void complete(boolean isMember) {
        this.isMember.complete(isMember);
    }
}
