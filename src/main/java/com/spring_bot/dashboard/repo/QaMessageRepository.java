package com.spring_bot.dashboard.repo;

import com.spring_bot.dashboard.model.QaMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class QaMessageRepository {
    private final JdbcTemplate jdbcTemplate;

    public QaMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(String channel, Long chatId, String username, String questionText, String answerText,
                     String sanitizedQuestion, String sanitizedAnswer) {
        jdbcTemplate.update("""
                insert into qa_message(channel, chat_id, username, question_text, answer_text, sanitized_question, sanitized_answer, created_at)
                values (?, ?, ?, ?, ?, ?, ?, current_timestamp)
                """, channel, chatId, username, questionText, answerText, sanitizedQuestion, sanitizedAnswer);
    }

    public List<QaMessage> findRecent(int limit) {
        return jdbcTemplate.query("""
                select id, channel, chat_id, username, question_text, answer_text, sanitized_question, sanitized_answer, created_at
                from qa_message
                order by created_at desc
                limit ?
                """, (rs, rowNum) -> new QaMessage(
                rs.getLong("id"),
                rs.getString("channel"),
                (Long) rs.getObject("chat_id"),
                rs.getString("username"),
                rs.getString("question_text"),
                rs.getString("answer_text"),
                rs.getString("sanitized_question"),
                rs.getString("sanitized_answer"),
                Optional.ofNullable(rs.getTimestamp("created_at")).map(Timestamp::toLocalDateTime).orElse(null)
        ), limit);
    }
}
