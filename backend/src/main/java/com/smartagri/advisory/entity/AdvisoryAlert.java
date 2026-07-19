package com.smartagri.advisory.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "advisory_alerts")
public class AdvisoryAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long fieldId;
    private String severity;
    private String title;
    @Column(length = 1000)
    private String message;
    private LocalDateTime createdAt;
    private Boolean dismissed = false;

    public Long getId()
    { return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getFieldId() {
        return fieldId;
    }
    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
    public String getSeverity() {
        return severity;
    }
    public void setSeverity(String severity)
    { this.severity = severity;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getMessage() {
         return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getDismissed() { return dismissed; }
    public void setDismissed(Boolean dismissed) { this.dismissed = dismissed; }
}
