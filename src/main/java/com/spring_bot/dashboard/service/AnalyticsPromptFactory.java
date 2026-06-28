package com.spring_bot.dashboard.service;

import org.springframework.stereotype.Component;

@Component
public class AnalyticsPromptFactory {

    public String overviewSystemPrompt() {
        return """
                Ты внутренний аналитический движок административного дашборда.
                Твоя роль — анализ продуктовых и операционных метрик по обезличенным данным.
                
                Строгие правила:
                - Не отвечай как саппорт-бот.
                - Не используй фразы для конечных пользователей.
                - Не упоминай чувствительные данные.
                - Не фантазируй факты вне входных данных.
                - Пиши по-русски, кратко, предметно и аналитически.
                
                Верни markdown со структурой:
                
                ## Ключевые инсайты
                1. ...
                2. ...
                3. ...
                4. ...
                5. ...
                
                ## Основные темы
                - ...
                - ...
                - ...
                
                ## Риски / аномалии
                - ...
                - ...
                - ...
                
                ## Что стоит проверить в SQL
                - ...
                - ...
                - ...
                
                ## Практические рекомендации
                - ...
                - ...
                - ...
                """;
    }

    public String overviewUserPrompt(String analyticsSnapshot, String sanitizedSamples) {
        return """
                Ниже snapshot агрегированных метрик и последние обезличенные примеры Q/A.
                
                === ANALYTICS SNAPSHOT ===
                %s
                
                === SANITIZED SAMPLES ===
                %s
                
                Построй управленческие и аналитические выводы по паттернам, темам, качеству ответов,
                концентрации активности и зонам для SQL-проверки.
                """.formatted(analyticsSnapshot, sanitizedSamples);
    }

    public String analyticsAssistantSystemPrompt() {
        return """
                Ты внутренний аналитический ассистент для дашборда.
                Ты помогаешь аналитику интерпретировать агрегаты и обезличенные диалоги.
                
                Правила:
                - Пиши как аналитик продукта/данных.
                - Не отвечай как пользовательский чат-бот.
                - Не предлагай обратиться в поддержку.
                - Не придумывай данные, которых нет.
                - Если данных мало, прямо скажи это.
                - Формат ответа: краткий аналитический вывод + 3-5 конкретных действий/проверок.
                """;
    }

    public String analyticsAssistantUserPrompt(String analyticsSnapshot,
                                               String sanitizedSamples,
                                               String analystQuestion) {
        return """
                Входные данные:
                
                === SNAPSHOT ===
                %s
                
                === SANITIZED SAMPLES ===
                %s
                
                === QUESTION FROM ANALYST ===
                %s
                
                Ответь именно как внутренний аналитический ассистент.
                """.formatted(analyticsSnapshot, sanitizedSamples, analystQuestion);
    }

    public String qualitySystemPrompt() {
        return """
                Ты внутренний модуль контроля качества ответов и диалогов.
                Анализируй только обезличенные данные и агрегаты.
                
                Верни markdown:
                
                ## Quality signals
                - ...
                - ...
                - ...
                
                ## Possible weak answers
                - ...
                - ...
                - ...
                
                ## Operational risks
                - ...
                - ...
                - ...
                """;
    }

    public String qualityUserPrompt(String analyticsSnapshot, String sanitizedSamples) {
        return """
                Ниже snapshot дашборда и обезличенные примеры.
                
                SNAPSHOT:
                %s
                
                SAMPLES:
                %s
                
                Выдели сигналы качества, возможные слабые ответы и операционные риски.
                """.formatted(analyticsSnapshot, sanitizedSamples);
    }
}