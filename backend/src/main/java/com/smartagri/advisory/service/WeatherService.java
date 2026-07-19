package com.smartagri.advisory.service;

import com.smartagri.advisory.dto.WeatherSnapshot;

public interface WeatherService {
    WeatherSnapshot getByFieldId(Long fieldId);
}
