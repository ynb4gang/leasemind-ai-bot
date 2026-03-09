package com.spring_bot.bot.model;

public enum UserState {
    IDLE("IDLE"),
    WAITING_FOR_EXPERT_QUESTION("WAITING_FOR_EXPERT_QUESTION"),
    WAITING_FOR_QUESTION("WAITING_FOR_QUESTION");

    private final String name;

    UserState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}