package com.spring_bot.dashboard.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> buildDashboardSummary() {
        long totalMessages = queryLong("""
                select count(*) from qa_message
                """);

        long totalUsers = queryLong("""
                select count(distinct coalesce(username, 'anonymous'))
                from qa_message
                """);

        long totalChats = queryLong("""
                select count(distinct chat_id)
                from qa_message
                where chat_id is not null
                """);

        long totalChannels = queryLong("""
                select count(distinct coalesce(channel, 'unknown'))
                from qa_message
                """);

        long messages24h = queryLong("""
                select count(*) from qa_message
                where created_at >= now() - interval '24 hours'
                """);

        long messages7d = queryLong("""
                select count(*) from qa_message
                where created_at >= now() - interval '7 days'
                """);

        long messages30d = queryLong("""
                select count(*) from qa_message
                where created_at >= now() - interval '30 days'
                """);

        double avgQuestionLength = queryDouble("""
                select coalesce(avg(length(coalesce(question_text, ''))), 0)
                from qa_message
                """);

        double avgAnswerLength = queryDouble("""
                select coalesce(avg(length(coalesce(answer_text, ''))), 0)
                from qa_message
                """);

        double answerCoverage = queryDouble("""
                select coalesce(
                    100.0 * sum(case when coalesce(answer_text, '') <> '' then 1 else 0 end) / nullif(count(*), 0),
                    0
                )
                from qa_message
                """);

        String peakHour = jdbcTemplate.query("""
                select extract(hour from created_at)::int as hour_of_day, count(*) as value
                from qa_message
                group by extract(hour from created_at)::int
                order by value desc, hour_of_day asc
                limit 1
                """, rs -> {
            if (!rs.next()) return "нет данных";
            int hour = rs.getInt("hour_of_day");
            return "%02d:00".formatted(hour);
        });

        LocalDateTime latestActivity = jdbcTemplate.query("""
                select max(created_at) as max_created_at
                from qa_message
                """, rs -> {
            if (!rs.next()) return null;
            Timestamp ts = rs.getTimestamp("max_created_at");
            return ts == null ? null : ts.toLocalDateTime();
        });

        List<Map<String, Object>> channelBreakdown = jdbcTemplate.queryForList("""
                select coalesce(channel, 'unknown') as channel,
                       count(*) as value
                from qa_message
                group by coalesce(channel, 'unknown')
                order by value desc, channel asc
                limit 8
                """);

        List<Map<String, Object>> topUsers = jdbcTemplate.queryForList("""
                select coalesce(username, 'anonymous') as username,
                       count(*) as value
                from qa_message
                group by coalesce(username, 'anonymous')
                order by value desc, username asc
                limit 8
                """);

        List<Map<String, Object>> dailyTrend = jdbcTemplate.query("""
                with days as (
                    select generate_series(current_date - interval '13 days', current_date, interval '1 day')::date as d
                )
                select d as day,
                       coalesce(q.cnt, 0) as value
                from days
                left join (
                    select created_at::date as day, count(*) as cnt
                    from qa_message
                    where created_at >= current_date - interval '13 days'
                    group by created_at::date
                ) q on q.day = d
                order by d
                """, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            Date day = rs.getDate("day");
            row.put("label", day == null ? null : day.toLocalDate().toString());
            row.put("value", rs.getLong("value"));
            return row;
        });

        List<Map<String, Object>> repeatedTopics = jdbcTemplate.query("""
                with prepared as (
                    select lower(trim(regexp_replace(coalesce(sanitized_question, question_text, ''), '\\s+', ' ', 'g'))) as topic
                    from qa_message
                )
                select topic,
                       count(*) as value
                from prepared
                where topic <> ''
                group by topic
                having count(*) >= 1
                order by value desc, topic asc
                limit 6
                """, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("topic", shorten(rs.getString("topic"), 90));
            row.put("value", rs.getLong("value"));
            return row;
        });

        List<Map<String, Object>> recentMessages = jdbcTemplate.queryForList("""
                select id,
                       channel,
                       chat_id,
                       username,
                       question_text,
                       answer_text,
                       sanitized_question,
                       sanitized_answer,
                       created_at
                from qa_message
                order by created_at desc
                limit 12
                """);

        List<String> heuristicInsights = buildHeuristicInsights(
                totalMessages, totalUsers, totalChats, totalChannels, messages24h, messages7d, messages30d,
                avgQuestionLength, avgAnswerLength, answerCoverage, peakHour, repeatedTopics
        );

        List<String> qualityFlags = buildQualityFlags(answerCoverage, avgAnswerLength, messages24h, latestActivity);

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalMessages", totalMessages);
        kpis.put("totalUsers", totalUsers);
        kpis.put("totalChats", totalChats);
        kpis.put("totalChannels", totalChannels);
        kpis.put("messages24h", messages24h);
        kpis.put("messages7d", messages7d);
        kpis.put("messages30d", messages30d);
        kpis.put("avgQuestionLength", round(avgQuestionLength));
        kpis.put("avgAnswerLength", round(avgAnswerLength));
        kpis.put("answerCoverage", round(answerCoverage));
        kpis.put("peakHour", peakHour);
        kpis.put("latestActivity", Optional.ofNullable(latestActivity)
                .map(LocalDateTime::toString)
                .orElse(null));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("kpis", kpis);
        result.put("channelBreakdown", channelBreakdown);
        result.put("topUsers", topUsers);
        result.put("dailyTrend", dailyTrend);
        result.put("repeatedTopics", repeatedTopics);
        result.put("recentMessages", recentMessages);
        result.put("heuristicInsights", heuristicInsights);
        result.put("qualityFlags", qualityFlags);
        return result;
    }

    public String analyticsSnapshotAsText() {
        Map<String, Object> summary = buildDashboardSummary();
        return summary.toString();
    }

    public String sanitizedSamplesAsText(int limit) {
        List<Map<String, Object>> rows = jdbcTemplate.query("""
                select channel,
                       username,
                       sanitized_question,
                       sanitized_answer,
                       created_at
                from qa_message
                where coalesce(sanitized_question, '') <> ''
                   or coalesce(sanitized_answer, '') <> ''
                order by created_at desc
                limit ?
                """, ps -> ps.setInt(1, limit), (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("channel", rs.getString("channel"));
            row.put("username", rs.getString("username"));
            row.put("sanitizedQuestion", shorten(rs.getString("sanitized_question"), 240));
            row.put("sanitizedAnswer", shorten(rs.getString("sanitized_answer"), 240));
            row.put("createdAt", Optional.ofNullable(rs.getTimestamp("created_at"))
                    .map(Timestamp::toLocalDateTime)
                    .map(LocalDateTime::toString)
                    .orElse(null));
            return row;
        });

        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> row : rows) {
            sb.append("- channel=").append(row.get("channel"))
                    .append(", username=").append(row.get("username"))
                    .append(", q=").append(row.get("sanitizedQuestion"))
                    .append(", a=").append(row.get("sanitizedAnswer"))
                    .append(", createdAt=").append(row.get("createdAt"))
                    .append("\n");
        }
        return sb.toString();
    }

    private List<String> buildHeuristicInsights(long totalMessages,
                                                long totalUsers,
                                                long totalChats,
                                                long totalChannels,
                                                long messages24h,
                                                long messages7d,
                                                long messages30d,
                                                double avgQuestionLength,
                                                double avgAnswerLength,
                                                double answerCoverage,
                                                String peakHour,
                                                List<Map<String, Object>> repeatedTopics) {
        String repeated = repeatedTopics.isEmpty()
                ? "нет устойчиво повторяющихся тем"
                : String.valueOf(repeatedTopics.getFirst().get("topic"));

        return List.of(
                "Общий объём диалогов: " + totalMessages + ". Уникальных пользователей: " + totalUsers + ", уникальных чатов: " + totalChats + ".",
                "Активность за 24 часа: " + messages24h + ", за 7 дней: " + messages7d + ", за 30 дней: " + messages30d + ".",
                "Средняя длина вопроса: " + round(avgQuestionLength) + " символов, средняя длина ответа: " + round(avgAnswerLength) + " символов.",
                "Покрытие ответами: " + round(answerCoverage) + "%. Пиковый час активности: " + peakHour + ".",
                "Повторяющаяся тема с наибольшей концентрацией: " + repeated + ". Активных каналов: " + totalChannels + "."
        );
    }

    private List<String> buildQualityFlags(double answerCoverage,
                                           double avgAnswerLength,
                                           long messages24h,
                                           LocalDateTime latestActivity) {
        String coverageFlag = answerCoverage < 85
                ? "Покрытие ответами ниже ожидаемого уровня."
                : "Покрытие ответами выглядит стабильным.";

        String answerLengthFlag = avgAnswerLength < 40
                ? "Средняя длина ответа низкая — возможны слишком короткие или поверхностные ответы."
                : "Средняя длина ответа выглядит приемлемой.";

        String freshnessFlag = latestActivity == null
                ? "Активность пока не зафиксирована."
                : "Последняя активность зафиксирована: " + latestActivity + ".";

        String loadFlag = messages24h == 0
                ? "За 24 часа нет новых записей."
                : "За последние 24 часа есть активность: " + messages24h + " записей.";

        return List.of(coverageFlag, answerLengthFlag, freshnessFlag, loadFlag);
    }

    private long queryLong(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }

    private double queryDouble(String sql) {
        Double value = jdbcTemplate.queryForObject(sql, Double.class);
        return value == null ? 0D : value;
    }

    private double round(double v) {
        return BigDecimal.valueOf(v)
                .setScale(1, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static String shorten(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }
}