package com.smartagri.advisory.service;

import com.smartagri.advisory.dto.RecommendationHistoryResponse;
import com.smartagri.advisory.dto.RecommendationResponse;

import java.util.List;

public interface RecommendationService {
    RecommendationResponse generateByFieldId(Long fieldId);

    List<RecommendationHistoryResponse> getHistoryByFieldId(Long fieldId);
}
