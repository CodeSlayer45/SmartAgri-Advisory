package com.smartagri.advisory.service.impl;

import com.smartagri.advisory.dto.RecommendationResponse;
import com.smartagri.advisory.entity.FarmField;
import com.smartagri.advisory.service.OpenAiWordingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OpenAiWordingServiceImpl implements OpenAiWordingService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiWordingServiceImpl.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.enabled:false}")
    private boolean enabled;

    @Value("${openai.api.model:gpt-4o-mini}")
    private String model;

    @Override
    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public Optional<String> buildFarmerAdvisory(FarmField field, RecommendationResponse ruleResult) {
        if (!isEnabled()) {
            return Optional.empty();
        }

        try {
            String userPrompt = buildPrompt(field, ruleResult);
            Map<String, Object> body = Map.of(
                    "model", model,
                    "temperature", 0.3,
                    "max_tokens", 450,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt()),
                            Map.of("role", "user", "content", userPrompt)
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Map<?, ?> responseBody = response.getBody();
            if (responseBody == null) {
                return Optional.empty();
            }

            List<?> choices = (List<?>) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                return Optional.empty();
            }

            Map<?, ?> first = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) first.get("message");
            if (message == null || message.get("content") == null) {
                return Optional.empty();
            }

            String content = String.valueOf(message.get("content")).trim();
            return content.isEmpty() ? Optional.empty() : Optional.of(content);
        } catch (Exception ex) {
            log.warn("OpenAI wording failed, using rule-only advisory: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private String systemPrompt() {
        return """
                You are a helpful agricultural advisor for smallholder farmers in India.
                You receive FACTS from a rule-based risk engine (risk level, score, reasons, actions).
                Write a short, clear advisory in plain English (3-5 short paragraphs or bullet groups).
                Rules:
                - Do NOT change the risk level or score.
                - Do NOT invent weather numbers or spray dates.
                - Keep the same actionable intent as the rule recommendations.
                - Be practical and calm; avoid alarmist language unless risk is HIGH.
                """;
    }

    private String bulletLines(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "- (none)";
        }
        return items.stream().map(item -> "- " + item).reduce((a, b) -> a + "\n" + b).orElse("- (none)");
    }

    private String buildPrompt(FarmField field, RecommendationResponse ruleResult) {
        return """
                Field: %s
                Crop: %s
                Location mode: %s
                Risk level: %s
                Risk score: %d

                Reasons:
                %s

                Rule recommendations:
                %s

                Write farmer-friendly advisory text based only on the above.
                """.formatted(
                field.getFieldName() != null ? field.getFieldName() : "Field",
                field.getCropName(),
                field.getLocationMode(),
                ruleResult.riskLevel(),
                ruleResult.riskScore(),
                bulletLines(ruleResult.explainableReasons()),
                bulletLines(ruleResult.recommendations())
        );
    }
}
