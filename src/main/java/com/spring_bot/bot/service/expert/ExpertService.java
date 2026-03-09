package com.spring_bot.bot.service.expert;

import com.spring_bot.bot.service.LocalizationService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExpertService {
    private static final List<String> EXPERT_IDS = List.of(
            "expert-1",
            "expert-2",
            "expert-3",
            "expert-4",
            "expert-5",
            "expert-6",
            "expert-7"
    );

    private final Map<String, Expert> expertsByLocale = new ConcurrentHashMap<>();
    private final LocalizationService localizationService;

    public ExpertService(LocalizationService localizationService) {
        this.localizationService = localizationService;
        initializeExperts();
    }

    private void initializeExperts() {
        // Предзагружаем экспертов для двух локалей
        for (Locale locale : Arrays.asList(Locale.of("ru"), Locale.of("en"))) {
            for (String expertId : EXPERT_IDS) {
                createExpertFromMessages(expertId, locale);
            }
        }
    }

    private Expert createExpertFromMessages(
            String expertId,
            Locale locale
    ) {
        try {
            String fullNameKey = "expert." + expertId + ".fullName";
            String bioKey = "expert." + expertId + ".bio";
            String contactsKey = "expert." + expertId + ".contacts";
            String personaPromptKey = "expert." + expertId + ".personaPrompt";
            String fullName = localizationService.getLocalizedMessage(fullNameKey, locale);
            String bio = localizationService.getLocalizedMessage(bioKey, locale);
            String contacts = localizationService.getLocalizedMessage(contactsKey, locale);
            String personaPrompt = localizationService.getLocalizedMessage(
                    personaPromptKey,
                    locale
            );
            String cacheKey = locale + "_" + expertId;
            Expert expert = new Expert(expertId, fullName, bio, contacts, personaPrompt);
            expertsByLocale.put(cacheKey, expert);
            return expert;
        }
        catch (Exception exceeption) {
            throw new RuntimeException(
                    "Failed to load expert " + expertId + " for locale " + locale,
                    exceeption
            );
        }
    }

    public List<Expert> listExperts(Locale locale) {
        return EXPERT_IDS.stream().map(id -> getExpertById(id, locale)).toList();
    }

    public Expert getExpertById(
            String id,
            Locale locale
    ) {
        String cacheKey = locale.toLanguageTag() + "_" + id;
        return expertsByLocale.computeIfAbsent(
                cacheKey,
                expert -> createExpertFromMessages(id, locale)
        );
    }
}
