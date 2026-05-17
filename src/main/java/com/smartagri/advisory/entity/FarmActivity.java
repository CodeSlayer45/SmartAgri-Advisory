package com.smartagri.advisory.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "farm_activities")
public class FarmActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long fieldId;
    private LocalDate activityDate;
    private String activityType;
    private String inputName;
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFieldId() { return fieldId; }
    public void setFieldId(Long fieldId) { this.fieldId = fieldId; }
    public LocalDate getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public String getInputName() { return inputName; }
    public void setInputName(String inputName) { this.inputName = inputName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
