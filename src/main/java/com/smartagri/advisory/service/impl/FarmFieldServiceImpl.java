package com.smartagri.advisory.service.impl;

import com.smartagri.advisory.dto.FarmFieldRequest;
import com.smartagri.advisory.dto.FarmFieldResponse;
import com.smartagri.advisory.dto.FieldLocationUpdateRequest;
import com.smartagri.advisory.entity.FarmField;
import com.smartagri.advisory.entity.LocationMode;
import com.smartagri.advisory.exception.ResourceNotFoundException;
import com.smartagri.advisory.mapper.FarmMapper;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.service.CropAgeService;
import com.smartagri.advisory.service.FarmFieldService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class FarmFieldServiceImpl implements FarmFieldService {
    private final FarmFieldRepository repository;
    private final FarmMapper mapper;
    private final CropAgeService cropAgeService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openweather.api.key:}")
    private String apiKey;

    public FarmFieldServiceImpl(FarmFieldRepository repository, FarmMapper mapper, CropAgeService cropAgeService) {
        this.repository = repository;
        this.mapper = mapper;
        this.cropAgeService = cropAgeService;
    }

    @Override
    public FarmFieldResponse create(FarmFieldRequest request) {
        FarmField entity = mapper.toEntity(request);
        applyLocationModeRules(
                entity,
                request.locationMode(),
                request.location(),
                request.latitude(),
                request.longitude()
        );
        FarmField saved = repository.save(entity);
        return mapper.toResponse(saved, cropAgeService.calculateDays(saved.getSowingDate()));
    }

    @Override
    public FarmFieldResponse updateLocation(Long id, FieldLocationUpdateRequest request) {
        FarmField field = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found for id: " + id));
        applyLocationModeRules(field, request.locationMode(), request.location(), request.latitude(), request.longitude());
        FarmField saved = repository.save(field);
        return mapper.toResponse(saved, cropAgeService.calculateDays(saved.getSowingDate()));
    }

    @Override
    public List<FarmFieldResponse> getAll() {
        return repository.findAll().stream()
                .map(f -> mapper.toResponse(f, cropAgeService.calculateDays(f.getSowingDate())))
                .toList();
    }

    @Override
    public FarmFieldResponse getById(Long id) {
        FarmField field = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found for id: " + id));
        return mapper.toResponse(field, cropAgeService.calculateDays(field.getSowingDate()));
    }

    private void applyLocationModeRules(FarmField field,
                                        LocationMode mode,
                                        String location,
                                        Double latitude,
                                        Double longitude) {
        if (mode == LocationMode.AUTO_GPS) {
            if (latitude == null || longitude == null) {
                throw new IllegalArgumentException("AUTO_GPS mode requires latitude and longitude.");
            }
            field.setLocationMode(mode);
            field.setLocation(location == null || location.isBlank() ? "GPS coordinates" : location);
            field.setLatitude(latitude);
            field.setLongitude(longitude);
            return;
        }

        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("MANUAL mode requires a location name.");
        }

        field.setLocationMode(mode);
        field.setLocation(location);
        double[] coords = geocodeLocation(location, latitude, longitude);
        field.setLatitude(coords[0]);
        field.setLongitude(coords[1]);
    }

    private double[] geocodeLocation(String location, Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            return new double[]{latitude, longitude};
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("OpenWeather API key is required for MANUAL mode without coordinates.");
        }

        String encodedLocation = UriUtils.encode(location, StandardCharsets.UTF_8);
        String url = "https://api.openweathermap.org/geo/1.0/direct?q=" + encodedLocation + "&limit=1&appid=" + apiKey;
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
        List results = response.getBody();
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("Could not resolve coordinates for location: " + location);
        }
        Map<String, Object> first = (Map<String, Object>) results.get(0);
        return new double[]{toDouble(first.get("lat")), toDouble(first.get("lon"))};
    }

    private double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        return Double.parseDouble(String.valueOf(value));
    }
}
