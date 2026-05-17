package com.smartagri.advisory.dto;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        Long fieldId,
        String severity,
        String title,
        String message,
        LocalDateTime createdAt
) {
}
