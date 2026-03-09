package com.spring_bot.bot.events;

import org.springframework.context.ApplicationEvent;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;

import java.io.Serializable;

public class MessageEvent extends ApplicationEvent {
    private final BotApiMethod<? extends Serializable> message;

    public MessageEvent(Object source, BotApiMethod<? extends Serializable> message) {
        super(source);
        this.message = message;
    }

    public BotApiMethod<? extends Serializable> getMessage() {
        return message;
    }
}
