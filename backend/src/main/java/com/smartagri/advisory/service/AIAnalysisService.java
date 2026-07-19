package com.smartagri.advisory.service;

import java.util.Map;

import com.smartagri.advisory.dto.WeatherSnapshot;
import com.smartagri.advisory.entity.FarmField;

public interface AIAnalysisService {
    /**
     * Analyze crop health from image (disease detection via AI vision)
     */
    Map<String, Object> analyzeCropHealth(Long fieldId, String imageBase64, String imageFormat);

    /**
     * Answer any farmer question in natural language, given field context
     */
    String askFarmerQuestion(Long fieldId, String question);

    /**
     * Predict market price trend for the farmer's crop
     */
    Map<String, Object> analyzeMarketPrice(String cropName, String location);

    /**
     * Generate weather impact analysis tailored to specific crop and growth stage
     */
    String analyzeWeatherImpact(FarmField field, WeatherSnapshot weather, long cropAgeDays);

    /**
     * Generate a complete growth stage report with specific actions
     */
    Map<String, Object> analyzeGrowthStage(FarmField field, long cropAgeDays);
}