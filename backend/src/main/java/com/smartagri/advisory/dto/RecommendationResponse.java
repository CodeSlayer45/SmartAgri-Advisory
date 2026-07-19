package com.smartagri.advisory.dto;

import java.util.List;

public record RecommendationResponse(
        Long fieldId,
        String riskLevel,
        int riskScore,
        List<String> explainableReasons,
        List<String> recommendations,
        boolean aiEnhanced,
        String farmerAdvisory
) {
    public static RecommendationResponse fromRules(
            Long fieldId,
            String riskLevel,
            int riskScore,
            List<String> explainableReasons,
            List<String> recommendations) {
        return new RecommendationResponse(
                fieldId, riskLevel, riskScore, explainableReasons, recommendations, false, null
        );
    }

    public RecommendationResponse withFarmerAdvisory(String advisory) {
        return new RecommendationResponse(
                fieldId, riskLevel, riskScore, explainableReasons, recommendations, true, advisory
        );
    }
}
