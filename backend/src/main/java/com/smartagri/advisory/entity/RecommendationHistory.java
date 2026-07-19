package com.smartagri.advisory.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation_history")
public class RecommendationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long fieldId;
    private String riskLevel;
    private Integer riskScore;
    private Boolean aiEnhanced;
    @Column(length = 2000)
    private String farmerAdvisory;
    @Column(length = 4000)
    private String explainableReasons;
    @Column(length = 4000)
    private String recommendations;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFieldId() { return fieldId; }
    public void setFieldId(Long fieldId) { this.fieldId = fieldId; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public Boolean getAiEnhanced() { return aiEnhanced; }
    public void setAiEnhanced(Boolean aiEnhanced) { this.aiEnhanced = aiEnhanced; }
    public String getFarmerAdvisory() { return farmerAdvisory; }
    public void setFarmerAdvisory(String farmerAdvisory) { this.farmerAdvisory = farmerAdvisory; }
    public String getExplainableReasons() { return explainableReasons; }
    public void setExplainableReasons(String explainableReasons) { this.explainableReasons = explainableReasons; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
