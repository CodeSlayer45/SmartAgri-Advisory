package com.smartagri.advisory.mapper;

import com.smartagri.advisory.dto.*;
import com.smartagri.advisory.entity.AdvisoryAlert;
import com.smartagri.advisory.entity.FarmActivity;
import com.smartagri.advisory.entity.FarmField;
import org.springframework.stereotype.Component;

@Component
public class FarmMapper {

    public FarmField toEntity(FarmFieldRequest request) {
        FarmField entity = new FarmField();
        entity.setFieldName(request.fieldName());
        entity.setCropName(request.cropName());
        entity.setAcreage(request.acreage());
        entity.setLocation(request.location());
        entity.setLocationMode(request.locationMode());
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        entity.setSowingDate(request.sowingDate());
        return entity;
    }

    public FarmFieldResponse toResponse(FarmField entity, long cropAgeDays) {
        return new FarmFieldResponse(
                entity.getId(), entity.getFieldName(), entity.getCropName(), entity.getAcreage(),
                entity.getLocation(), entity.getLocationMode(), entity.getLatitude(),
                entity.getLongitude(), entity.getSowingDate(), cropAgeDays
        );
    }

    public FarmActivity toEntity(FarmActivityRequest request) {
        FarmActivity entity = new FarmActivity();
        entity.setFieldId(request.fieldId());
        entity.setActivityDate(request.activityDate());
        entity.setActivityType(request.activityType());
        entity.setInputName(request.inputName());
        entity.setNotes(request.notes());
        return entity;
    }

    public FarmActivityResponse toResponse(FarmActivity entity) {
        return new FarmActivityResponse(
                entity.getId(), entity.getFieldId(), entity.getActivityDate(),
                entity.getActivityType(), entity.getInputName(), entity.getNotes()
        );
    }

    public AlertResponse toResponse(AdvisoryAlert alert) {
        return new AlertResponse(
                alert.getId(), alert.getFieldId(), alert.getSeverity(),
                alert.getTitle(), alert.getMessage(), alert.getCreatedAt()
        );
    }
}
