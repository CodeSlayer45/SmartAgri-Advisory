package com.smartagri.advisory.dto;

import jakarta.validation.constraints.*;
import com.smartagri.advisory.entity.LocationMode;

import java.time.LocalDate;

public record FarmFieldRequest(
        @NotBlank String fieldName,
        @NotBlank String cropName,
        @NotNull @Positive Double acreage,
        String location,
        @NotNull LocationMode locationMode,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @NotNull LocalDate sowingDate
) {
}
