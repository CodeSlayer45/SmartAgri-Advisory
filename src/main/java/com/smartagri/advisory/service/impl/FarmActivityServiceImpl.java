package com.smartagri.advisory.service.impl;

import com.smartagri.advisory.dto.FarmActivityRequest;
import com.smartagri.advisory.dto.FarmActivityResponse;
import com.smartagri.advisory.entity.FarmActivity;
import com.smartagri.advisory.exception.ResourceNotFoundException;
import com.smartagri.advisory.mapper.FarmMapper;
import com.smartagri.advisory.repository.FarmActivityRepository;
import com.smartagri.advisory.repository.FarmFieldRepository;
import com.smartagri.advisory.service.FarmActivityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FarmActivityServiceImpl implements FarmActivityService {
    private final FarmActivityRepository activityRepository;
    private final FarmFieldRepository fieldRepository;
    private final FarmMapper mapper;

    public FarmActivityServiceImpl(FarmActivityRepository activityRepository, FarmFieldRepository fieldRepository, FarmMapper mapper) {
        this.activityRepository = activityRepository;
        this.fieldRepository = fieldRepository;
        this.mapper = mapper;
    }

    @Override
    public FarmActivityResponse create(FarmActivityRequest request) {
        if (!fieldRepository.existsById(request.fieldId())) {
            throw new ResourceNotFoundException("Field not found for id: " + request.fieldId());
        }
        FarmActivity saved = activityRepository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Override
    public List<FarmActivityResponse> getByFieldId(Long fieldId) {
        return activityRepository.findByFieldId(fieldId).stream().map(mapper::toResponse).toList();
    }
}
