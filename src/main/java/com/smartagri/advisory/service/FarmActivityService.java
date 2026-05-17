package com.smartagri.advisory.service;

import com.smartagri.advisory.dto.FarmActivityRequest;
import com.smartagri.advisory.dto.FarmActivityResponse;

import java.util.List;

public interface FarmActivityService {
    FarmActivityResponse create(FarmActivityRequest request);
    List<FarmActivityResponse> getByFieldId(Long fieldId);
}
