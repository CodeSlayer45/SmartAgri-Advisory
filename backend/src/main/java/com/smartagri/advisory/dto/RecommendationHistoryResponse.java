package com.smartagri.advisory.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecommendationHistoryResponse(
        Long id,
        Long fieldId,
        String riskLevel,
        int riskScore,
        boolean aiEnhanced,
        String farmerAdvisory,
        List<String> explainableReasons,
        List<String> recommendations,
        LocalDateTime createdAt
) {
}
