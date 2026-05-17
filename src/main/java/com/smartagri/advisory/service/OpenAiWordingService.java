package com.smartagri.advisory.service;

import com.smartagri.advisory.dto.RecommendationResponse;
import com.smartagri.advisory.entity.FarmField;

import java.util.Optional;

public interface OpenAiWordingService {
    boolean isEnabled();

    Optional<String> buildFarmerAdvisory(FarmField field, RecommendationResponse ruleResult);
}
