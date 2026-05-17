package com.smartagri.advisory.dto;

import com.smartagri.advisory.entity.LocationMode;

import java.time.LocalDate;

public record FarmFieldResponse(
        Long id,
        String fieldName,
        String cropName,
        Double acreage,
        String location,
        LocationMode locationMode,
        Double latitude,
        Double longitude,
        LocalDate sowingDate,
        long cropAgeDays
) {
}
