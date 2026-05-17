package com.smartagri.advisory.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CropAgeServiceTest {

    private final CropAgeService cropAgeService = new CropAgeService();

    @Test
    void shouldCalculateDaysFromSowingDateToToday() {
        LocalDate sowing = LocalDate.now().minusDays(37);
        assertEquals(37, cropAgeService.calculateDays(sowing));
    }

    @Test
    void shouldReturnZeroForSowingToday() {
        assertEquals(0, cropAgeService.calculateDays(LocalDate.now()));
    }
}
