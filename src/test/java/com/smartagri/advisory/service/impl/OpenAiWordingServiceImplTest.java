package com.smartagri.advisory.service.impl;

import com.smartagri.advisory.dto.RecommendationResponse;
import com.smartagri.advisory.entity.FarmField;
import com.smartagri.advisory.entity.LocationMode;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiWordingServiceImplTest {

  @Test
  void shouldBeDisabledWhenKeyMissing() {
    OpenAiWordingServiceImpl service = new OpenAiWordingServiceImpl();
    ReflectionTestUtils.setField(service, "enabled", true);
    ReflectionTestUtils.setField(service, "apiKey", "");

    assertFalse(service.isEnabled());
    FarmField field = sampleField();
    RecommendationResponse rules = RecommendationResponse.fromRules(
        1L, "LOW", 20, List.of("Mild weather"), List.of("Monitor crop"));
    assertTrue(service.buildFarmerAdvisory(field, rules).isEmpty());
  }

  @Test
  void shouldBeDisabledWhenFlagOff() {
    OpenAiWordingServiceImpl service = new OpenAiWordingServiceImpl();
    ReflectionTestUtils.setField(service, "enabled", false);
    ReflectionTestUtils.setField(service, "apiKey", "sk-test");

    assertFalse(service.isEnabled());
  }

  private FarmField sampleField() {
    FarmField field = new FarmField();
    field.setId(1L);
    field.setFieldName("Demo");
    field.setCropName("Wheat");
    field.setLocationMode(LocationMode.AUTO_GPS);
    return field;
  }
}
