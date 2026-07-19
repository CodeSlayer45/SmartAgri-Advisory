package com.smartagri.advisory.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.smartagri.advisory.dto.RecommendationHistoryResponse;
import com.smartagri.advisory.dto.RecommendationResponse;
import com.smartagri.advisory.dto.WeatherSnapshot;
import com.smartagri.advisory.entity.FarmActivity;
import com.smartagri.advisory.entity.FarmField;
import com.smartagri.advisory.entity.RecommendationHistory;
import com.smartagri.advisory.exception.ResourceNotFoundException;
import com.smartagri.advisory.repository.FarmActivityRepository;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.repository.RecommendationHistoryRepository;
import com.smartagri.advisory.service.CropAgeService;
import com.smartagri.advisory.service.OpenAiWordingService;
import com.smartagri.advisory.service.WeatherService;

@Service
public class RecommendationServiceImpl implements com.smartagri.advisory.service.RecommendationService {
    private final FarmFieldRepository fieldRepository;
    private final FarmActivityRepository activityRepository;
    private final WeatherService weatherService;
    private final CropAgeService cropAgeService;
    private final OpenAiWordingService openAiWordingService;
    private final RecommendationHistoryRepository historyRepository;

    public RecommendationServiceImpl(FarmFieldRepository fieldRepository,
                                     FarmActivityRepository activityRepository,
                                     WeatherService weatherService,
                                     CropAgeService cropAgeService,
                                     OpenAiWordingService openAiWordingService,
                                     RecommendationHistoryRepository historyRepository) {
        this.fieldRepository = fieldRepository;
        this.activityRepository = activityRepository;
        this.weatherService = weatherService;
        this.cropAgeService = cropAgeService;
        this.openAiWordingService = openAiWordingService;
        this.historyRepository = historyRepository;
    }

    public RecommendationResponse generateByFieldId(Long fieldId) {
        FarmField field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found for id: " + fieldId));
        WeatherSnapshot weather = weatherService.getByFieldId(fieldId);
        List<FarmActivity> activities = activityRepository.findByFieldId(fieldId);
        long cropAge = cropAgeService.calculateDays(field.getSowingDate());

        int riskScore = 0;
        List<String> reasons = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (weather.humidityPercent() >= 80) { riskScore += 25; reasons.add("Humidity is above 80%, creating fungal-friendly conditions."); }
        if (weather.rainfallMm() > 0) { riskScore += 20; reasons.add("Recent rainfall can increase leaf wetness and disease risk."); }
        if (weather.temperatureC() >= 22 && weather.temperatureC() <= 30) { riskScore += 10; reasons.add("Temperature is in a fungal growth favorable range."); }

        long daysSinceFungicide = daysSinceLastSpray(activities, "fungicide");
        if (daysSinceFungicide > 10) { riskScore += 30; reasons.add("No fungicide spray recorded in last " + daysSinceFungicide + " days."); recommendations.add("Apply preventive fungicide spray in next 24-48 hours."); }
        else if (daysSinceFungicide >= 0) { reasons.add("Last fungicide spray was " + daysSinceFungicide + " days ago."); }
        else { riskScore += 20; reasons.add("No fungicide history found for this field."); recommendations.add("Create a baseline preventive spray plan and log all applications."); }

        if (cropAge <= 20) recommendations.add("Maintain light irrigation and monitor early stage growth stress.");
        else if (cropAge <= 50) recommendations.add("Ensure balanced nutrient schedule in active vegetative stage.");
        else recommendations.add("Prioritize flowering/fruit-stage disease scouting and canopy airflow.");

        if (weather.rainfallMm() > 5) recommendations.add("Avoid immediate over-irrigation after rain; reassess soil moisture first.");

        String riskLevel = riskScore >= 70 ? "HIGH" : riskScore >= 40 ? "MEDIUM" : "LOW";
        if ("HIGH".equals(riskLevel)) recommendations.add(0, "High disease risk detected. Perform field inspection today.");

        RecommendationResponse ruleResult = RecommendationResponse.fromRules(fieldId, riskLevel, Math.min(riskScore, 100), reasons, recommendations);
        RecommendationResponse finalResult = openAiWordingService.buildFarmerAdvisory(field, ruleResult)
                .map(ruleResult::withFarmerAdvisory).orElse(ruleResult);
        saveHistory(finalResult);
        return finalResult;
    }

    public List<RecommendationHistoryResponse> getHistoryByFieldId(Long fieldId) {
        if (!fieldRepository.existsById(fieldId)) throw new ResourceNotFoundException("Field not found for id: " + fieldId);
        return historyRepository.findTop10ByFieldIdOrderByCreatedAtDesc(fieldId).stream()
                .map(e -> new RecommendationHistoryResponse(e.getId(), e.getFieldId(), e.getRiskLevel(),
                        e.getRiskScore() == null ? 0 : e.getRiskScore(),
                        Boolean.TRUE.equals(e.getAiEnhanced()), e.getFarmerAdvisory(),
                        e.getExplainableReasons() == null ? Collections.emptyList() : Arrays.asList(e.getExplainableReasons().split("\u001e", -1)),
                        e.getRecommendations() == null ? Collections.emptyList() : Arrays.asList(e.getRecommendations().split("\u001e", -1)),
                        e.getCreatedAt()))
                .toList();
    }

    private void saveHistory(RecommendationResponse response) {
        RecommendationHistory entity = new RecommendationHistory();
        entity.setFieldId(response.fieldId());
        entity.setRiskLevel(response.riskLevel());
        entity.setRiskScore(response.riskScore());
        entity.setAiEnhanced(response.aiEnhanced());
        entity.setFarmerAdvisory(response.farmerAdvisory());
        entity.setExplainableReasons(response.explainableReasons() == null ? "" : String.join("\u001e", response.explainableReasons()));
        entity.setRecommendations(response.recommendations() == null ? "" : String.join("\u001e", response.recommendations()));
        entity.setCreatedAt(LocalDateTime.now());
        historyRepository.save(entity);
    }

    // TODO: Make thresholds configurable per crop type (wheat vs rice need different humidity levels)
    // TODO: Add fertilizer-specific recommendations based on crop growth stage
    // FIXME: This doesn't handle the case where activityDate is in the future (bad data entry)
    private long daysSinceLastSpray(List<FarmActivity> activities, String type) {
        Optional<LocalDate> lastDate = activities.stream()
                .filter(a -> a.getActivityType() != null && a.getActivityType().equalsIgnoreCase(type))
                .map(FarmActivity::getActivityDate).max(Comparator.naturalOrder());
        return lastDate.map(d -> ChronoUnit.DAYS.between(d, LocalDate.now())).orElse(-1L);
    }
}