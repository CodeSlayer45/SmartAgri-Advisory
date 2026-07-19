package com.smartagri.advisory.controller;

import com.smartagri.advisory.dto.RecommendationHistoryResponse;
import com.smartagri.advisory.dto.RecommendationResponse;
import com.smartagri.advisory.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/recommend/history/{fieldId}")
    public List<RecommendationHistoryResponse> history(@PathVariable Long fieldId) {
        return recommendationService.getHistoryByFieldId(fieldId);
    }
}
