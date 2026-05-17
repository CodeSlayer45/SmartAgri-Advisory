package com.smartagri.advisory.controller;

import com.smartagri.advisory.dto.WeatherSnapshot;
import com.smartagri.advisory.service.WeatherService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/field/{fieldId}")
    public WeatherSnapshot getByField(@PathVariable Long fieldId) {
        return weatherService.getByFieldId(fieldId);
    }
}
