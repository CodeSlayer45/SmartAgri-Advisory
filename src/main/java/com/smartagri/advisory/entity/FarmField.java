package com.smartagri.advisory.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "farm_fields")
public class FarmField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fieldName;
    private String cropName;
    private Double acreage;
    private String location;
    @Enumerated(EnumType.STRING)
    private LocationMode locationMode;
    private Double latitude;
    private Double longitude;
    private LocalDate sowingDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public Double getAcreage() { return acreage; }
    public void setAcreage(Double acreage) { this.acreage = acreage; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocationMode getLocationMode() { return locationMode; }
    public void setLocationMode(LocationMode locationMode) { this.locationMode = locationMode; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public LocalDate getSowingDate() { return sowingDate; }
    public void setSowingDate(LocalDate sowingDate) { this.sowingDate = sowingDate; }
}
