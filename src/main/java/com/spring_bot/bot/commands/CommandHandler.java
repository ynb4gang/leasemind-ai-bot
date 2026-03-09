package com.spring_bot.bot.commands;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collection;

@Service
public class CommandHandler {
    private final Collection<Command> commands;

    public CommandHandler(Collection<Command> commands) {
        this.commands = commands;
    }

    public void handle(Update update) {
        for (Command command : commands) {
            if (command.canHandle(update)) {
                command.handle(update);
                return;
            }
        }
    }
}