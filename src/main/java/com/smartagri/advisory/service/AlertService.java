package com.smartagri.advisory.service;

import com.smartagri.advisory.dto.AlertResponse;

import java.util.List;

public interface AlertService {
    void evaluateAndStoreAlerts();
    List<AlertResponse> getByFieldId(Long fieldId);
}
