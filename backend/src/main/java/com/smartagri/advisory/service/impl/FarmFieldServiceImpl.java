package com.smartagri.advisory.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.smartagri.advisory.dto.FarmFieldRequest;
import com.smartagri.advisory.dto.FarmFieldResponse;
import com.smartagri.advisory.dto.FarmFieldUpdateRequest;
import com.smartagri.advisory.dto.FieldLocationUpdateRequest;
import com.smartagri.advisory.entity.FarmField;
import com.smartagri.advisory.entity.LocationMode;
import com.smartagri.advisory.exception.ResourceNotFoundException;
import com.smartagri.advisory.repository.AdvisoryAlertRepository;
import com.smartagri.advisory.repository.FarmActivityRepository;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.repository.RecommendationHistoryRepository;
import com.smartagri.advisory.service.CropAgeService;
import com.smartagri.advisory.service.FarmFieldService;

@Service
public class FarmFieldServiceImpl implements FarmFieldService {
    private final FarmFieldRepository repository;
    private final FarmActivityRepository activityRepository;
    private final AdvisoryAlertRepository alertRepository;
    private final RecommendationHistoryRepository historyRepository;
    private final CropAgeService cropAgeService;

    @Value("${openweather.api.key:}")
    private String apiKey;
    private final RestTemplate rt = new RestTemplate();

    public FarmFieldServiceImpl(FarmFieldRepository repository, FarmActivityRepository activityRepository,
                                AdvisoryAlertRepository alertRepository, RecommendationHistoryRepository historyRepository,
                                CropAgeService cropAgeService) {
        this.repository = repository; this.activityRepository = activityRepository;
        this.alertRepository = alertRepository; this.historyRepository = historyRepository; this.cropAgeService = cropAgeService;
    }

    private FarmFieldResponse toResponse(FarmField f) {
        return new FarmFieldResponse(f.getId(), f.getFieldName(), f.getCropName(), f.getAcreage(),
                f.getLocation(), f.getLocationMode(), f.getLatitude(), f.getLongitude(), f.getSowingDate(),
                cropAgeService.calculateDays(f.getSowingDate()));
    }

    @Override public FarmFieldResponse create(FarmFieldRequest r) {
        FarmField e = new FarmField();
        e.setFieldName(r.fieldName()); e.setCropName(r.cropName()); e.setAcreage(r.acreage());
        e.setLocation(r.location()); e.setLocationMode(r.locationMode());
        e.setLatitude(r.latitude()); e.setLongitude(r.longitude()); e.setSowingDate(r.sowingDate());
        applyLocation(e, r.locationMode(), r.location(), r.latitude(), r.longitude());
        return toResponse(repository.save(e));
    }

    @Override public FarmFieldResponse update(Long id, FarmFieldUpdateRequest r) {
        FarmField f = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Field not found: " + id));
        f.setFieldName(r.fieldName()); f.setCropName(r.cropName()); f.setAcreage(r.acreage()); f.setSowingDate(r.sowingDate());
        return toResponse(repository.save(f));
    }

    @Override public FarmFieldResponse updateLocation(Long id, FieldLocationUpdateRequest r) {
        FarmField f = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Field not found: " + id));
        applyLocation(f, r.locationMode(), r.location(), r.latitude(), r.longitude());
        return toResponse(repository.save(f));
    }

    @Override public List<FarmFieldResponse> getAll() { return repository.findAll().stream().map(this::toResponse).toList(); }
    @Override public FarmFieldResponse getById(Long id) {
        return toResponse(repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Field not found: " + id)));
    }

    @Override @Transactional public void deleteById(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Field not found: " + id);
        alertRepository.deleteByFieldId(id); activityRepository.deleteByFieldId(id); historyRepository.deleteByFieldId(id);
        repository.deleteById(id);
    }

    private void applyLocation(FarmField f, LocationMode mode, String loc, Double lat, Double lng) {
        if (mode == LocationMode.AUTO_GPS) {
            if (lat == null || lng == null) throw new IllegalArgumentException("AUTO_GPS needs coordinates.");
            f.setLocationMode(mode); f.setLocation(loc != null && !loc.isBlank() ? loc : "GPS coordinates");
            f.setLatitude(lat); f.setLongitude(lng); return;
        }
        if (loc == null || loc.isBlank()) throw new IllegalArgumentException("MANUAL mode needs a location name.");
        f.setLocationMode(mode); f.setLocation(loc);
        double[] c = geocode(loc, lat, lng);
        f.setLatitude(c[0]); f.setLongitude(c[1]);
    }

    private double[] geocode(String location, Double lat, Double lng) {
        if (lat != null && lng != null) return new double[]{lat, lng};
        if (apiKey == null || apiKey.isBlank()) throw new IllegalArgumentException("API key needed for geocoding.");
        String url = "https://api.openweathermap.org/geo/1.0/direct?q=" + UriUtils.encode(location, StandardCharsets.UTF_8) + "&limit=1&appid=" + apiKey;
        ResponseEntity<List> res = rt.getForEntity(url, List.class);
        List results = res.getBody();
        if (results == null || results.isEmpty()) throw new IllegalArgumentException("Could not resolve: " + location);
        Map<String, Object> first = (Map<String, Object>) results.get(0);
        return new double[]{((Number)first.get("lat")).doubleValue(), ((Number)first.get("lon")).doubleValue()};
    }
}