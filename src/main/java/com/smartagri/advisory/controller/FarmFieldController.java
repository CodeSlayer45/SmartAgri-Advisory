package com.smartagri.advisory.controller;

import com.smartagri.advisory.dto.FarmFieldRequest;
import com.smartagri.advisory.dto.FarmFieldResponse;
import com.smartagri.advisory.dto.FieldLocationUpdateRequest;
import com.smartagri.advisory.service.FarmFieldService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/field")
public class FarmFieldController {
    private final FarmFieldService service;

    public FarmFieldController(FarmFieldService service) {
        this.service = service;
    }

    @PostMapping
    public FarmFieldResponse create(@Valid @RequestBody FarmFieldRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<FarmFieldResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{fieldId}")
    public FarmFieldResponse getById(@PathVariable Long fieldId) {
        return service.getById(fieldId);
    }

    @PatchMapping("/{fieldId}/location")
    public FarmFieldResponse updateLocation(@PathVariable Long fieldId, @Valid @RequestBody FieldLocationUpdateRequest request) {
        return service.updateLocation(fieldId, request);
    }
}
