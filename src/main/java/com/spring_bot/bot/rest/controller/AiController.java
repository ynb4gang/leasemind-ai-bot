package com.spring_bot.bot.rest.controller;

import com.spring_bot.bot.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/fact")
    public String randomFact(@RequestParam(required = false, defaultValue = "ru") String locale) {
        return aiService.randomFact(Locale.of(locale));
    }

    @PostMapping("/question")
    public String answerQuestion(
            @RequestBody String question,
            @RequestParam(required = false, defaultValue = "ru") String locale
    ) {
        return aiService.answerQuestion(question, Locale.of(locale));
    }

    @PostMapping("/ask-expert")
    public String answerAsExpert(
            @RequestParam String expertId,
            @RequestBody String question,
            @RequestParam(required = false, defaultValue = "ru") String locale
    ) {
        return aiService.answerAsExpert(expertId, question, Locale.of(locale));
    }
}