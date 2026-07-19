package com.smartagri.advisory.service;

import com.smartagri.advisory.dto.FarmFieldRequest;
import com.smartagri.advisory.dto.FarmFieldResponse;
import com.smartagri.advisory.dto.FarmFieldUpdateRequest;
import com.smartagri.advisory.dto.FieldLocationUpdateRequest;

import java.util.List;

public interface FarmFieldService {
    FarmFieldResponse create(FarmFieldRequest request);
    FarmFieldResponse update(Long id, FarmFieldUpdateRequest request);

    FarmFieldResponse updateLocation(Long id, FieldLocationUpdateRequest request);
    List<FarmFieldResponse> getAll();
    FarmFieldResponse getById(Long id);

    void deleteById(Long id);
}
