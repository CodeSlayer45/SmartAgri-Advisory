package com.smartagri.advisory.dto;

public record AIAnalysisRequest(
    Long fieldId,
    String imageBase64,
    String imageFormat,
    String question
) {}