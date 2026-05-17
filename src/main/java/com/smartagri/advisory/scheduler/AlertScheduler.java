package com.smartagri.advisory.scheduler;

import com.smartagri.advisory.service.AlertService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertScheduler {
    private final AlertService alertService;

    public AlertScheduler(AlertService alertService) {
        this.alertService = alertService;
    }

    @Scheduled(cron = "${advisory.alert.cron:0 0 */6 * * *}")
    public void runWeatherDiseaseAlertScan() {
        alertService.evaluateAndStoreAlerts();
    }
}
