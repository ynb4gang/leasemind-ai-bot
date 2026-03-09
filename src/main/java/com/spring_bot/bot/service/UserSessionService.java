package com.spring_bot.bot.service;

import com.spring_bot.bot.model.UserSession;
import com.spring_bot.bot.model.UserState;
import com.spring_bot.bot.model.repo.UserSessionRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserSessionService {
    private final UserSessionRepository userSessionRepository;

    public UserSessionService(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    private UserSession getOrCreateUserSession(Long chatId) {
        Optional<UserSession> userSession = userSessionRepository.findByChatId(chatId);
        return userSession.orElseGet(() -> {
            UserSession newSession = new UserSession();
            newSession.setChatId(chatId);
            newSession.setUserState(UserState.IDLE);
            newSession.setLocale("ru");
            return userSessionRepository.save(newSession);
        });
    }

    public Locale getLocale(Long chatId) {
        return Locale.forLanguageTag(getOrCreateUserSession(chatId).getLocale());
    }

    public void setLocale(
            Long chatId,
            String locale
    ) {
        UserSession userSession = getOrCreateUserSession(chatId);
        userSession.setLocale(locale);
        userSessionRepository.save(userSession);
    }

    public void setSelectedExpertId(
            Long chatId,
            String expertId
    ) {
        UserSession userSession = getOrCreateUserSession(chatId);
        userSession.setSelectedExpertId(expertId);
        userSessionRepository.save(userSession);
    }

    public void setUserState(
            Long chatId,
            UserState userState
    ) {
        UserSession userSession = getOrCreateUserSession(chatId);
        userSession.setUserState(userState);
        userSessionRepository.save(userSession);
    }

    public String getSelectedExpertId(Long chatId) {
        return getOrCreateUserSession(chatId).getSelectedExpertId();
    }

    public UserState getUserState(Long chatId) {
        return getOrCreateUserSession(chatId).getUserState();
    }

    public boolean isPromocodeActivated(Long chatId) {
        UserSession userSession = getOrCreateUserSession(chatId);
        return userSession.getIsPromoActivated() != null
                && userSession.getIsPromoActivated();
    }

    public void setPromoActivated(Long chatId) {
        UserSession userSession = getOrCreateUserSession(chatId);
        userSession.setIsPromoActivated(true);
        userSessionRepository.save(userSession);
    }
}