package com.smartagri.advisory.service.impl;

import com.smartagri.advisory.dto.WeatherSnapshot;
import com.smartagri.advisory.entity.FarmField;
import com.smartagri.advisory.exception.ResourceNotFoundException;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.service.WeatherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class WeatherServiceImpl implements WeatherService {
    private final FarmFieldRepository fieldRepository;
    private final RestTemplate restTemplate;

    @Value("${openweather.api.key:}")
    private String apiKey;

    public WeatherServiceImpl(FarmFieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public WeatherSnapshot getByFieldId(Long fieldId) {
        FarmField field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found for id: " + fieldId));
        if (field.getLatitude() == null || field.getLongitude() == null) {
            throw new IllegalArgumentException("Field coordinates are missing. Update field location first.");
        }

        if (apiKey == null || apiKey.isBlank()) {
            return new WeatherSnapshot("mock", "API key missing, using fallback", 28.0, 78, 0.0, 2.4);
        }

        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric",
                field.getLatitude(), field.getLongitude(), apiKey);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map body = response.getBody();
        if (body == null) {
            return new WeatherSnapshot("openweather", "No weather payload", 0.0, 0, 0.0, 0.0);
        }

        Map<String, Object> main = (Map<String, Object>) body.getOrDefault("main", Map.of());
        Map<String, Object> wind = (Map<String, Object>) body.getOrDefault("wind", Map.of());
        Map<String, Object> rain = (Map<String, Object>) body.getOrDefault("rain", Map.of());
        List<Map<String, Object>> weather = (List<Map<String, Object>>) body.getOrDefault("weather", List.of());
        String summary = weather.isEmpty() ? "n/a" : String.valueOf(weather.get(0).getOrDefault("description", "n/a"));
        double rainfall = toDouble(rain.getOrDefault("1h", 0.0));

        return new WeatherSnapshot(
                "openweather",
                summary,
                toDouble(main.getOrDefault("temp", 0.0)),
                (int) Math.round(toDouble(main.getOrDefault("humidity", 0))),
                rainfall,
                toDouble(wind.getOrDefault("speed", 0.0))
        );
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number n) return n.doubleValue();
        return Double.parseDouble(String.valueOf(value));
    }
}
