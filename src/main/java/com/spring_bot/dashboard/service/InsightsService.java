package com.spring_bot.dashboard.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InsightsService {

    private final AnalyticsService analyticsService;
    private final AnalyticsAiService analyticsAiService;

    public InsightsService(AnalyticsService analyticsService,
                           AnalyticsAiService analyticsAiService) {
        this.analyticsService = analyticsService;
        this.analyticsAiService = analyticsAiService;
    }

    public Map<String, Object> generateInsightsPack() {
        Map<String, Object> analytics = analyticsService.buildDashboardSummary();
        String snapshot = analyticsService.analyticsSnapshotAsText();
        String samples = analyticsService.sanitizedSamplesAsText(40);

        String insights = analyticsAiService.generateOverviewInsights(snapshot, samples);
        String qualitySummary = analyticsAiService.generateQualitySummary(snapshot, samples);

        return Map.of(
                "analyticsSnapshot", analytics,
                "sampleSize", samples.isBlank() ? 0 : samples.split("\n").length,
                "insights", insights == null ? "Недостаточно данных для генерации инсайтов." : insights,
                "qualitySummary", qualitySummary == null ? "Недостаточно данных для оценки качества." : qualitySummary
        );
    }
}