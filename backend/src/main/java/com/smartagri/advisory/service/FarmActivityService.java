package com.smartagri.advisory.service;

import com.smartagri.advisory.dto.FarmActivityRequest;
import com.smartagri.advisory.dto.FarmActivityResponse;
import com.smartagri.advisory.dto.FarmActivityUpdateRequest;

import java.util.List;

public interface FarmActivityService {
    FarmActivityResponse create(FarmActivityRequest request);
    List<FarmActivityResponse> getByFieldId(Long fieldId);

    FarmActivityResponse update(Long activityId, FarmActivityUpdateRequest request);

    void deleteById(Long activityId);
}
