package com.spring_bot.bot.service.expert;

public class Expert {
    private final String id;
    private final String fullName;
    private final String bio;
    private final String contacts;
    private final String personaPrompt;

    public Expert(
            String id,
            String fullName,
            String bio,
            String contacts,
            String personaPrompt
    ) {
        this.id = id;
        this.fullName = fullName;
        this.bio = bio;
        this.contacts = contacts;
        this.personaPrompt = personaPrompt;
    }

    @Override
    public String toString() {
        return "%s. %s. %s".formatted(fullName, bio, contacts);
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPersonaPrompt() {
        return personaPrompt;
    }
}