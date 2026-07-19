package com.smartagri.advisory.dto;

import com.smartagri.advisory.entity.LocationMode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record FieldLocationUpdateRequest(
        @NotNull LocationMode locationMode,
        String location,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude
) {
}
