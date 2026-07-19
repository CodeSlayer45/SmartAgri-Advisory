package com.smartagri.advisory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record FarmFieldUpdateRequest(
        @NotBlank String fieldName,
        @NotBlank String cropName,
        @NotNull @Positive Double acreage,
        @NotNull LocalDate sowingDate
) {
}
