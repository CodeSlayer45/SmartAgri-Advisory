package com.smartagri.advisory.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartagri.advisory.dto.FarmActivityRequest;
import com.smartagri.advisory.dto.FarmActivityResponse;
import com.smartagri.advisory.dto.FarmActivityUpdateRequest;
import com.smartagri.advisory.entity.FarmActivity;
import com.smartagri.advisory.exception.ResourceNotFoundException;
import com.smartagri.advisory.repository.FarmActivityRepository;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.service.FarmActivityService;

@Service
public class FarmActivityServiceImpl implements FarmActivityService {
    private final FarmActivityRepository activityRepository;
    private final FarmFieldRepository fieldRepository;

    public FarmActivityServiceImpl(FarmActivityRepository activityRepository, FarmFieldRepository fieldRepository) {
        this.activityRepository = activityRepository;
        this.fieldRepository = fieldRepository;
    }

    @Override
    public FarmActivityResponse create(FarmActivityRequest request) {
        if (!fieldRepository.existsById(request.fieldId()))
            throw new ResourceNotFoundException("Field not found for id: " + request.fieldId());
        FarmActivity e = new FarmActivity();
        e.setFieldId(request.fieldId()); e.setActivityDate(request.activityDate());
        e.setActivityType(request.activityType()); e.setInputName(request.inputName()); e.setNotes(request.notes());
        FarmActivity saved = activityRepository.save(e);
        return new FarmActivityResponse(saved.getId(), saved.getFieldId(), saved.getActivityDate(),
                saved.getActivityType(), saved.getInputName(), saved.getNotes());
    }

    @Override
    public List<FarmActivityResponse> getByFieldId(Long fieldId) {
        return activityRepository.findByFieldId(fieldId).stream()
                .map(a -> new FarmActivityResponse(a.getId(), a.getFieldId(), a.getActivityDate(),
                        a.getActivityType(), a.getInputName(), a.getNotes())).toList();
    }

    @Override
    public FarmActivityResponse update(Long activityId, FarmActivityUpdateRequest request) {
        FarmActivity a = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));
        a.setActivityDate(request.activityDate()); a.setActivityType(request.activityType());
        a.setInputName(request.inputName()); a.setNotes(request.notes());
        FarmActivity saved = activityRepository.save(a);
        return new FarmActivityResponse(saved.getId(), saved.getFieldId(), saved.getActivityDate(),
                saved.getActivityType(), saved.getInputName(), saved.getNotes());
    }

    @Override
    public void deleteById(Long activityId) {
        FarmActivity a = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));
        activityRepository.delete(a);
    }
}