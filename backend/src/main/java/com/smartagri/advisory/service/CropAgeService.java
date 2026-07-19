package com.smartagri.advisory.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class CropAgeService {
    public long calculateDays(LocalDate sowingDate) {
        return ChronoUnit.DAYS.between(sowingDate, LocalDate.now());
    }
}
