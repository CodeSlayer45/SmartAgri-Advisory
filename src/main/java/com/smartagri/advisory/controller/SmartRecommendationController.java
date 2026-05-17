package com.smartagri.advisory.controller;

import com.smartagri.advisory.dto.RecommendationResponse;
import com.smartagri.advisory.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/smart")
public class SmartRecommendationController {
    private final RecommendationService recommendationService;

    public SmartRecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/recommend/{fieldId}")
    public RecommendationResponse recommend(@PathVariable Long fieldId) {
        return recommendationService.generateByFieldId(fieldId);
    }
}
