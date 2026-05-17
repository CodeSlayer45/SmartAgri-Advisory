package com.smartagri.advisory.controller;

import com.smartagri.advisory.dto.AlertResponse;
import com.smartagri.advisory.service.AlertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/field/{fieldId}")
    public List<AlertResponse> byField(@PathVariable Long fieldId) {
        return alertService.getByFieldId(fieldId);
    }

    @PostMapping("/scan")
    public Map<String, String> triggerScan() {
        alertService.evaluateAndStoreAlerts();
        return Map.of("status", "completed", "message", "Alert scan completed for all fields");
    }
}
