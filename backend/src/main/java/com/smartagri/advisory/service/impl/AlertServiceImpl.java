package com.smartagri.advisory.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartagri.advisory.dto.AlertResponse;
import com.smartagri.advisory.dto.RecommendationResponse;
import com.smartagri.advisory.entity.AdvisoryAlert;
import com.smartagri.advisory.exception.ResourceNotFoundException;
import com.smartagri.advisory.repository.AdvisoryAlertRepository;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.service.AlertService;
import com.smartagri.advisory.service.RecommendationService;

@Service
public class AlertServiceImpl implements AlertService {
    private final FarmFieldRepository fieldRepository;
    private final RecommendationService recommendationService;
    private final AdvisoryAlertRepository alertRepository;

    public AlertServiceImpl(FarmFieldRepository fieldRepository, RecommendationService recommendationService,
                            AdvisoryAlertRepository alertRepository) {
        this.fieldRepository = fieldRepository;
        this.recommendationService = recommendationService;
        this.alertRepository = alertRepository;
    }

    @Override
    public void evaluateAndStoreAlerts() {
        fieldRepository.findAll().forEach(field -> {
            RecommendationResponse response = recommendationService.generateByFieldId(field.getId());
            if ("HIGH".equals(response.riskLevel()) || response.riskScore() >= 65) {
                boolean recentAlert = alertRepository.findByFieldIdOrderByCreatedAtDesc(field.getId())
                        .stream().anyMatch(a -> a.getCreatedAt() != null
                                && a.getCreatedAt().isAfter(LocalDateTime.now().minusHours(6)));
                if (recentAlert) return;
                AdvisoryAlert alert = new AdvisoryAlert();
                alert.setFieldId(field.getId()); alert.setSeverity("HIGH");
                alert.setTitle("Disease risk alert for " + field.getFieldName());
                alert.setMessage(String.join(" ", response.explainableReasons()));
                alert.setCreatedAt(LocalDateTime.now());
                alertRepository.save(alert);
            }
        });
    }

    @Override
    public List<AlertResponse> getByFieldId(Long fieldId) {
        return alertRepository.findByFieldIdAndDismissedFalseOrderByCreatedAtDesc(fieldId).stream()
                .map(a -> new AlertResponse(a.getId(), a.getFieldId(), a.getSeverity(),
                        a.getTitle(), a.getMessage(), a.getCreatedAt(),
                        Boolean.TRUE.equals(a.getDismissed()))).toList();
    }

    @Override
    public void dismiss(Long alertId) {
        AdvisoryAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + alertId));
        alert.setDismissed(true);
        alertRepository.save(alert);
    }
}