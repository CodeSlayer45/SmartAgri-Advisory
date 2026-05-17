package com.smartagri.advisory.service;

import com.smartagri.advisory.dto.RecommendationResponse;

public interface RecommendationService {
    RecommendationResponse generateByFieldId(Long fieldId);
}
