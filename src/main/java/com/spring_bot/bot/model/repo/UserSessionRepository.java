package com.spring_bot.bot.model.repo;
import com.spring_bot.bot.model.UserSession ;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface UserSessionRepository extends ListCrudRepository<UserSession, Long> {
    Optional<UserSession> findByChatId(Long chatId);
}