package com.smartagri.advisory.service.impl;

import com.smartagri.advisory.dto.RecommendationResponse;
import com.smartagri.advisory.dto.WeatherSnapshot;
import com.smartagri.advisory.entity.FarmActivity;
import com.smartagri.advisory.entity.FarmField;
import com.smartagri.advisory.exception.ResourceNotFoundException;
import com.smartagri.advisory.repository.FarmActivityRepository;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.service.CropAgeService;
import com.smartagri.advisory.service.OpenAiWordingService;
import com.smartagri.advisory.service.RecommendationService;
import com.smartagri.advisory.service.WeatherService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private final FarmFieldRepository fieldRepository;
    private final FarmActivityRepository activityRepository;
    private final WeatherService weatherService;
    private final CropAgeService cropAgeService;
    private final OpenAiWordingService openAiWordingService;

    public RecommendationServiceImpl(FarmFieldRepository fieldRepository,
                                     FarmActivityRepository activityRepository,
                                     WeatherService weatherService,
                                     CropAgeService cropAgeService,
                                     OpenAiWordingService openAiWordingService) {
        this.fieldRepository = fieldRepository;
        this.activityRepository = activityRepository;
        this.weatherService = weatherService;
        this.cropAgeService = cropAgeService;
        this.openAiWordingService = openAiWordingService;
    }

    @Override
    public RecommendationResponse generateByFieldId(Long fieldId) {
        FarmField field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found for id: " + fieldId));
        WeatherSnapshot weather = weatherService.getByFieldId(fieldId);
        List<FarmActivity> activities = activityRepository.findByFieldId(fieldId);
        long cropAge = cropAgeService.calculateDays(field.getSowingDate());

        int riskScore = 0;
        List<String> reasons = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        if (weather.humidityPercent() >= 80) {
            riskScore += 25;
            reasons.add("Humidity is above 80%, creating fungal-friendly conditions.");
        }
        if (weather.rainfallMm() > 0) {
            riskScore += 20;
            reasons.add("Recent rainfall can increase leaf wetness and disease risk.");
        }
        if (weather.temperatureC() >= 22 && weather.temperatureC() <= 30) {
            riskScore += 10;
            reasons.add("Temperature is in a fungal growth favorable range.");
        }

        long daysSinceFungicide = daysSinceLastSpray(activities, "fungicide");
        if (daysSinceFungicide > 10) {
            riskScore += 30;
            reasons.add("No fungicide spray recorded in last " + daysSinceFungicide + " days.");
            recommendations.add("Apply preventive fungicide spray in next 24-48 hours.");
        } else if (daysSinceFungicide >= 0) {
            reasons.add("Last fungicide spray was " + daysSinceFungicide + " days ago.");
        } else {
            riskScore += 20;
            reasons.add("No fungicide history found for this field.");
            recommendations.add("Create a baseline preventive spray plan and log all applications.");
        }

        if (cropAge <= 20) {
            recommendations.add("Maintain light irrigation and monitor early stage growth stress.");
        } else if (cropAge <= 50) {
            recommendations.add("Ensure balanced nutrient schedule in active vegetative stage.");
        } else {
            recommendations.add("Prioritize flowering/fruit-stage disease scouting and canopy airflow.");
        }

        if (weather.rainfallMm() > 5) {
            recommendations.add("Avoid immediate over-irrigation after rain; reassess soil moisture first.");
        }

        String riskLevel = riskScore >= 70 ? "HIGH" : riskScore >= 40 ? "MEDIUM" : "LOW";
        if ("HIGH".equals(riskLevel)) {
            recommendations.add(0, "High disease risk detected. Perform field inspection today.");
        }

        RecommendationResponse ruleResult = RecommendationResponse.fromRules(
                fieldId, riskLevel, Math.min(riskScore, 100), reasons, recommendations
        );

        return openAiWordingService.buildFarmerAdvisory(field, ruleResult)
                .map(ruleResult::withFarmerAdvisory)
                .orElse(ruleResult);
    }

    private long daysSinceLastSpray(List<FarmActivity> activities, String type) {
        Optional<LocalDate> lastDate = activities.stream()
                .filter(a -> a.getActivityType() != null && a.getActivityType().equalsIgnoreCase(type))
                .map(FarmActivity::getActivityDate)
                .max(Comparator.naturalOrder());
        return lastDate.map(localDate -> ChronoUnit.DAYS.between(localDate, LocalDate.now())).orElse(-1L);
    }
}
