package com.smartagri.advisory.dto;

import java.time.LocalDate;

public record FarmActivityResponse(
        Long id,
        Long fieldId,
        LocalDate activityDate,
        String activityType,
        String inputName,
        String notes
) {
}
