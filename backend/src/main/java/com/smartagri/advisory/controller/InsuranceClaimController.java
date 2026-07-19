package com.smartagri.advisory.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartagri.advisory.entity.FarmField;
import com.smartagri.advisory.entity.InsuranceClaim;
import com.smartagri.advisory.exception.ResourceNotFoundException;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.repository.InsuranceClaimRepository;

@RestController
@RequestMapping("/api/insurance")
public class InsuranceClaimController {

    private final InsuranceClaimRepository claimRepository;
    private final FarmFieldRepository fieldRepository;

    public InsuranceClaimController(InsuranceClaimRepository claimRepository,
                                    FarmFieldRepository fieldRepository) {
        this.claimRepository = claimRepository;
        this.fieldRepository = fieldRepository;
    }

    @PostMapping("/claim/{fieldId}")
    public ResponseEntity<Map<String, Object>> autoFileClaim(@PathVariable Long fieldId) {
        FarmField field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found: " + fieldId));

        // Build weather data snapshot
        String weatherSnapshot = String.format(
                "Temperature: %.1f°C, Humidity: %d%%, Rainfall: %.1fmm, Date: %s",
                28.0, 78, 0.0, LocalDate.now()
        );

        InsuranceClaim claim = new InsuranceClaim(
                field,
                "Heavy rainfall detected",
                50.0,
                35.0,
                weatherSnapshot
        );
        claimRepository.save(claim);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "success");
        result.put("claimReference", claim.getClaimReference());
        result.put("claimStatus", claim.getStatus().toString());
        result.put("filedAt", claim.getFiledAt().toString());
        result.put("fieldName", field.getFieldName());
        result.put("cropName", field.getCropName());
        result.put("triggerEvent", claim.getTriggerEvent());
        result.put("message", "✅ Insurance claim auto-filed. Reference: " + claim.getClaimReference());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/claims/{fieldId}")
    public ResponseEntity<List<Map<String, Object>>> getClaims(@PathVariable Long fieldId) {
        List<InsuranceClaim> claims = claimRepository.findByFieldIdOrderByFiledAtDesc(fieldId);
        List<Map<String, Object>> result = claims.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("claimReference", c.getClaimReference());
            m.put("status", c.getStatus().toString());
            m.put("triggerEvent", c.getTriggerEvent());
            m.put("filedAt", c.getFiledAt().toString());
            m.put("submittedToInsurer", c.isSubmittedToInsurer());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }
}