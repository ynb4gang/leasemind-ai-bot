package com.spring_bot.dashboard.controller;

import com.spring_bot.dashboard.dto.AiQuestionRequest;
import com.spring_bot.dashboard.dto.SqlRequest;
import com.spring_bot.dashboard.model.AppRole;
import com.spring_bot.dashboard.model.AppUser;
import com.spring_bot.dashboard.service.AnalyticsAiService;
import com.spring_bot.dashboard.service.AnalyticsService;
import com.spring_bot.dashboard.service.AuthService;
import com.spring_bot.dashboard.service.InsightsService;
import com.spring_bot.dashboard.service.QaAuditService;
import com.spring_bot.dashboard.service.SqlConsoleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/dashboard/api")
public class DashboardController {

    private final AuthService authService;
    private final AnalyticsService analyticsService;
    private final AnalyticsAiService analyticsAiService;
    private final InsightsService insightsService;
    private final SqlConsoleService sqlConsoleService;
    private final QaAuditService qaAuditService;

    public DashboardController(AuthService authService,
                               AnalyticsService analyticsService,
                               AnalyticsAiService analyticsAiService,
                               InsightsService insightsService,
                               SqlConsoleService sqlConsoleService,
                               QaAuditService qaAuditService) {
        this.authService = authService;
        this.analyticsService = analyticsService;
        this.analyticsAiService = analyticsAiService;
        this.insightsService = insightsService;
        this.sqlConsoleService = sqlConsoleService;
        this.qaAuditService = qaAuditService;
    }

    @GetMapping("/analytics/summary")
    public Map<String, Object> analytics(HttpSession session) {
        AppUser user = authService.requireUser(session);
        authService.requireAnyRole(user, AppRole.ADMIN, AppRole.ANALYST, AppRole.VIEWER);
        return analyticsService.buildDashboardSummary();
    }

    @PostMapping("/analytics/ask")
    public Map<String, Object> analyticsAsk(@RequestBody AiQuestionRequest request, HttpSession session) {
        AppUser user = authService.requireUser(session);
        authService.requireAnyRole(user, AppRole.ADMIN, AppRole.ANALYST);

        String snapshot = analyticsService.analyticsSnapshotAsText();
        String samples = analyticsService.sanitizedSamplesAsText(30);

        String answer = analyticsAiService.answerAnalyticsQuestion(
                snapshot,
                samples,
                request.question()
        );

        qaAuditService.saveExchange(
                "dashboard-analytics",
                null,
                user.username(),
                request.question(),
                answer
        );

        return Map.of("answer", answer == null ? "Недостаточно данных для аналитического ответа." : answer);
    }

    @GetMapping("/insights")
    public Map<String, Object> insights(HttpSession session) {
        AppUser user = authService.requireUser(session);
        authService.requireAnyRole(user, AppRole.ADMIN, AppRole.ANALYST, AppRole.VIEWER);
        return insightsService.generateInsightsPack();
    }

    @PostMapping("/sql/query")
    public Map<String, Object> sql(@RequestBody SqlRequest request, HttpSession session) {
        AppUser user = authService.requireUser(session);
        authService.requireAnyRole(user, AppRole.ADMIN, AppRole.ANALYST);
        return sqlConsoleService.executeReadOnly(request.sql(), request.limit());
    }

    @GetMapping("/sql/tables")
    public Object tables(HttpSession session) {
        AppUser user = authService.requireUser(session);
        authService.requireAnyRole(user, AppRole.ADMIN, AppRole.ANALYST);
        return sqlConsoleService.tables();
    }

    @GetMapping("/sql/columns")
    public Object columns(@RequestParam String schema,
                          @RequestParam String table,
                          HttpSession session) {
        AppUser user = authService.requireUser(session);
        authService.requireAnyRole(user, AppRole.ADMIN, AppRole.ANALYST);
        return sqlConsoleService.columns(schema, table);
    }

    @GetMapping("/sql/preview")
    public Map<String, Object> preview(@RequestParam String schema,
                                       @RequestParam String table,
                                       @RequestParam(defaultValue = "50") Integer limit,
                                       HttpSession session) {
        AppUser user = authService.requireUser(session);
        authService.requireAnyRole(user, AppRole.ADMIN, AppRole.ANALYST);
        return sqlConsoleService.preview(schema, table, limit);
    }
}