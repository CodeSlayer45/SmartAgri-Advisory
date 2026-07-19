package com.smartagri.advisory.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.smartagri.advisory.dto.WeatherSnapshot;
import com.smartagri.advisory.entity.FarmField;
import com.smartagri.advisory.exception.ResourceNotFoundException;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.service.WeatherService;

@Service
public class WeatherServiceImpl implements WeatherService {
    private final FarmFieldRepository fieldRepository;
    @Value("${openweather.api.key:}") private String apiKey;
    private final RestTemplate rt = new RestTemplate();

    public WeatherServiceImpl(FarmFieldRepository fieldRepository) { this.fieldRepository = fieldRepository; }

    @Override
    public WeatherSnapshot getByFieldId(Long fieldId) {
        FarmField f = fieldRepository.findById(fieldId).orElseThrow(() -> new ResourceNotFoundException("Field not found: " + fieldId));
        if (f.getLatitude() == null || f.getLongitude() == null) throw new IllegalArgumentException("Coordinates missing.");
        if (apiKey == null || apiKey.isBlank()) return new WeatherSnapshot("mock", "API key missing, fallback", 28.0, 78, 0.0, 2.4);
        ResponseEntity<Map> res = rt.getForEntity(String.format("https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric", f.getLatitude(), f.getLongitude(), apiKey), Map.class);
        Map body = res.getBody();
        if (body == null) return new WeatherSnapshot("openweather", "No data", 0.0, 0, 0.0, 0.0);
        Map<String, Object> m = (Map<String, Object>) body.getOrDefault("main", Map.of());
        Map<String, Object> w = (Map<String, Object>) body.getOrDefault("wind", Map.of());
        Map<String, Object> r = (Map<String, Object>) body.getOrDefault("rain", Map.of());
        List<Map<String, Object>> we = (List<Map<String, Object>>) body.getOrDefault("weather", List.of());
        return new WeatherSnapshot("openweather", we.isEmpty() ? "n/a" : String.valueOf(we.get(0).getOrDefault("description", "n/a")),
                toD(m.get("temp")), (int) Math.round(toD(m.get("humidity"))), toD(r.get("1h")), toD(w.get("speed")));
    }

    // HACK: OpenWeather free tier limits to 60 calls/min — consider caching with TTL if usage grows
    // TODO: Add geocoding fallback — if coordinates are missing, try city name lookup
    private double toD(Object v) { if (v == null) return 0.0; if (v instanceof Number n) return n.doubleValue(); return Double.parseDouble(v.toString()); }
}
