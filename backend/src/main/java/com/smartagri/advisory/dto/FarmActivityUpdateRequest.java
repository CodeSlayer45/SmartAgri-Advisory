package com.smartagri.advisory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FarmActivityUpdateRequest(
        @NotNull LocalDate activityDate,
        @NotBlank String activityType,
        String inputName,
        String notes
) {
}
