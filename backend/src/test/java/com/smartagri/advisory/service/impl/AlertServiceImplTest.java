package com.smartagri.advisory.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartagri.advisory.dto.RecommendationResponse;
import com.smartagri.advisory.entity.AdvisoryAlert;
import com.smartagri.advisory.entity.FarmField;
import com.smartagri.advisory.repository.AdvisoryAlertRepository;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.service.RecommendationService;

@ExtendWith(MockitoExtension.class)
class AlertServiceImplTest {

    @Mock private FarmFieldRepository fieldRepository;
    @Mock private RecommendationService recommendationService;
    @Mock private AdvisoryAlertRepository alertRepository;

    @InjectMocks
    private AlertServiceImpl alertService;

    @Test
    void shouldStoreAlertWhenRiskIsHighAndNoRecentAlert() {
        FarmField field = new FarmField();
        field.setId(1L); field.setFieldName("Demo");

        when(fieldRepository.findAll()).thenReturn(List.of(field));
        when(recommendationService.generateByFieldId(1L)).thenReturn(
                RecommendationResponse.fromRules(1L, "HIGH", 80, List.of("High humidity"), List.of("Spray"))
        );
        when(alertRepository.findByFieldIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        alertService.evaluateAndStoreAlerts();

        verify(alertRepository).save(any(AdvisoryAlert.class));
    }

    @Test
    void shouldSkipAlertWhenRecentAlertExists() {
        FarmField field = new FarmField();
        field.setId(1L); field.setFieldName("Demo");

        AdvisoryAlert recent = new AdvisoryAlert();
        recent.setCreatedAt(LocalDateTime.now().minusHours(2));

        when(fieldRepository.findAll()).thenReturn(List.of(field));
        when(recommendationService.generateByFieldId(1L)).thenReturn(
                RecommendationResponse.fromRules(1L, "HIGH", 80, List.of("High humidity"), List.of("Spray"))
        );
        when(alertRepository.findByFieldIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(recent));

        alertService.evaluateAndStoreAlerts();

        verify(alertRepository, never()).save(any());
    }
}