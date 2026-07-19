package com.smartagri.advisory.dto;

public record WeatherSnapshot(
        String source,
        String summary,
        double temperatureC,
        int humidityPercent,
        double rainfallMm,
        double windSpeedMs
) {
}
