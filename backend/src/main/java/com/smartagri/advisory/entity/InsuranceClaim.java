package com.smartagri.advisory.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "insurance_claims")
public class InsuranceClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private FarmField field;

    @Column(name = "claim_reference", unique = true, length = 100)
    private String claimReference;

    @Column(name = "trigger_event", length = 255)
    private String triggerEvent;

    @Column(name = "trigger_value")
    private Double triggerValue;

    @Column(name = "threshold_value")
    private Double thresholdValue;

    @Column(name = "filed_at", nullable = false)
    private LocalDateTime filedAt;

    @Column(name = "weather_data_snapshot", columnDefinition = "TEXT")
    private String weatherDataSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimStatus status;

    @Column(name = "submitted_to_insurer", nullable = false)
    private boolean submittedToInsurer;

    @Column(length = 500)
    private String notes;

    public enum ClaimStatus {
        AUTO_FILED, SUBMITTED, APPROVED, REJECTED
    }

    public InsuranceClaim() {}

    public InsuranceClaim(FarmField field, String triggerEvent, Double triggerValue,
                          Double thresholdValue, String weatherDataSnapshot) {
        this.field = field;
        this.triggerEvent = triggerEvent;
        this.triggerValue = triggerValue;
        this.thresholdValue = thresholdValue;
        this.weatherDataSnapshot = weatherDataSnapshot;
        this.filedAt = LocalDateTime.now();
        this.status = ClaimStatus.AUTO_FILED;
        this.submittedToInsurer = false;
        this.claimReference = "CLM-" + java.time.LocalDate.now().toString().replace("-", "") + "-"
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public FarmField getField() { return field; }
    public void setField(FarmField field) { this.field = field; }
    public String getClaimReference() { return claimReference; }
    public void setClaimReference(String claimReference) { this.claimReference = claimReference; }
    public String getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(String triggerEvent) { this.triggerEvent = triggerEvent; }
    public Double getTriggerValue() { return triggerValue; }
    public void setTriggerValue(Double triggerValue) { this.triggerValue = triggerValue; }
    public Double getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(Double thresholdValue) { this.thresholdValue = thresholdValue; }
    public LocalDateTime getFiledAt() { return filedAt; }
    public void setFiledAt(LocalDateTime filedAt) { this.filedAt = filedAt; }
    public String getWeatherDataSnapshot() { return weatherDataSnapshot; }
    public void setWeatherDataSnapshot(String weatherDataSnapshot) { this.weatherDataSnapshot = weatherDataSnapshot; }
    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }
    public boolean isSubmittedToInsurer() { return submittedToInsurer; }
    public void setSubmittedToInsurer(boolean submittedToInsurer) { this.submittedToInsurer = submittedToInsurer; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}