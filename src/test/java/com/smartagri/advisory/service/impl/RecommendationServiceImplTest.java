package com.smartagri.advisory.service.impl;

import com.smartagri.advisory.dto.RecommendationResponse;
import com.smartagri.advisory.dto.WeatherSnapshot;
import com.smartagri.advisory.entity.FarmActivity;
import com.smartagri.advisory.entity.FarmField;
import com.smartagri.advisory.exception.ResourceNotFoundException;
import com.smartagri.advisory.repository.FarmActivityRepository;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.service.CropAgeService;
import com.smartagri.advisory.service.OpenAiWordingService;
import com.smartagri.advisory.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock
    private FarmFieldRepository fieldRepository;
    @Mock
    private FarmActivityRepository activityRepository;
    @Mock
    private WeatherService weatherService;
    @Mock
    private CropAgeService cropAgeService;
    @Mock
    private OpenAiWordingService openAiWordingService;

    private RecommendationServiceImpl recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationServiceImpl(
                fieldRepository, activityRepository, weatherService, cropAgeService, openAiWordingService
        );
    }

    @Test
    void shouldReturnHighRiskWhenHumidityRainAndSprayDelayAreHigh() {
        Long fieldId = 1L;
        LocalDate sowingDate = LocalDate.now().minusDays(40);
        FarmField field = buildField(fieldId, sowingDate);

        FarmActivity oldFungicide = new FarmActivity();
        oldFungicide.setActivityType("fungicide");
        oldFungicide.setActivityDate(LocalDate.now().minusDays(15));

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
        when(weatherService.getByFieldId(fieldId)).thenReturn(
                new WeatherSnapshot("test", "rainy", 26.0, 88, 8.0, 2.0)
        );
        when(activityRepository.findByFieldId(fieldId)).thenReturn(List.of(oldFungicide));
        when(cropAgeService.calculateDays(sowingDate)).thenReturn(40L);

        RecommendationResponse response = recommendationService.generateByFieldId(fieldId);

        assertEquals("HIGH", response.riskLevel());
        assertEquals(85, response.riskScore());
        assertTrue(response.explainableReasons().stream().anyMatch(r -> r.contains("Humidity is above 80%")));
        assertTrue(response.explainableReasons().stream().anyMatch(r -> r.contains("No fungicide spray recorded")));
        assertEquals("High disease risk detected. Perform field inspection today.", response.recommendations().get(0));
    }

    @Test
    void shouldReturnLowRiskWhenNoFungicideHistoryButMildWeather() {
        Long fieldId = 2L;
        LocalDate sowingDate = LocalDate.now().minusDays(10);
        FarmField field = buildField(fieldId, sowingDate);

        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
        when(weatherService.getByFieldId(fieldId)).thenReturn(
                new WeatherSnapshot("test", "clear", 24.0, 60, 0.0, 1.2)
        );
        when(activityRepository.findByFieldId(fieldId)).thenReturn(List.of());
        when(cropAgeService.calculateDays(sowingDate)).thenReturn(10L);

        RecommendationResponse response = recommendationService.generateByFieldId(fieldId);

        assertEquals("LOW", response.riskLevel());
        assertEquals(30, response.riskScore());
        assertTrue(response.explainableReasons().stream().anyMatch(r -> r.contains("No fungicide history found")));
        assertTrue(response.recommendations().stream().anyMatch(r -> r.contains("baseline preventive spray plan")));
    }

    @Test
    void shouldThrowWhenFieldNotFound() {
        Long fieldId = 999L;
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recommendationService.generateByFieldId(fieldId));
    }

    private FarmField buildField(Long id, LocalDate sowingDate) {
        FarmField field = new FarmField();
        field.setId(id);
        field.setFieldName("Field " + id);
        field.setCropName("Wheat");
        field.setSowingDate(sowingDate);
        return field;
    }
}
