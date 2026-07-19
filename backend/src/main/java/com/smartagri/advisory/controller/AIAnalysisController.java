package com.smartagri.advisory.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartagri.advisory.dto.AIAnalysisRequest;
import com.smartagri.advisory.dto.WeatherSnapshot;
import com.smartagri.advisory.entity.FarmField;
import com.smartagri.advisory.exception.ResourceNotFoundException;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.service.AIAnalysisService;
import com.smartagri.advisory.service.WeatherService;

@RestController
@RequestMapping("/api/ai")
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;
    private final FarmFieldRepository fieldRepository;
    private final WeatherService weatherService;

    public AIAnalysisController(AIAnalysisService aiAnalysisService,
                                FarmFieldRepository fieldRepository,
                                WeatherService weatherService) {
        this.aiAnalysisService = aiAnalysisService;
        this.fieldRepository = fieldRepository;
        this.weatherService = weatherService;
    }

    @PostMapping("/analyze-crop-health")
    public ResponseEntity<Map<String, Object>> analyzeCropHealth(@RequestBody AIAnalysisRequest request) {
        Map<String, Object> result = aiAnalysisService.analyzeCropHealth(
                request.fieldId(), request.imageBase64(), request.imageFormat());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askQuestion(@RequestBody AIAnalysisRequest request) {
        String answer = aiAnalysisService.askFarmerQuestion(request.fieldId(), request.question());
        return ResponseEntity.ok(Map.of(
                "fieldId", String.valueOf(request.fieldId()),
                "question", request.question(),
                "answer", answer
        ));
    }

    @GetMapping("/market-price")
    public ResponseEntity<Map<String, Object>> marketPrice(
            @RequestParam String crop,
            @RequestParam(required = false) String location) {
        Map<String, Object> result = aiAnalysisService.analyzeMarketPrice(crop, location);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/weather-impact/{fieldId}")
    public ResponseEntity<Map<String, Object>> weatherImpact(@PathVariable Long fieldId) {
        FarmField field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found: " + fieldId));
        WeatherSnapshot weather = weatherService.getByFieldId(fieldId);
        long cropAge = field.getSowingDate() == null ? 0 :
                ChronoUnit.DAYS.between(field.getSowingDate(), LocalDate.now());

        String analysis = aiAnalysisService.analyzeWeatherImpact(field, weather, cropAge);
        return ResponseEntity.ok(Map.of(
                "fieldId", fieldId,
                "crop", field.getCropName(),
                "weather", weather,
                "analysis", analysis
        ));
    }

    @GetMapping("/growth-stage/{fieldId}")
    public ResponseEntity<Map<String, Object>> growthStage(@PathVariable Long fieldId) {
        FarmField field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found: " + fieldId));
        long cropAge = field.getSowingDate() == null ? 0 :
                ChronoUnit.DAYS.between(field.getSowingDate(), LocalDate.now());

        Map<String, Object> result = aiAnalysisService.analyzeGrowthStage(field, cropAge);
        return ResponseEntity.ok(result);
    }
}
