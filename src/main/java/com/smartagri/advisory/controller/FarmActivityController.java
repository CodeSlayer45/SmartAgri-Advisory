package com.smartagri.advisory.controller;

import com.smartagri.advisory.dto.FarmActivityRequest;
import com.smartagri.advisory.dto.FarmActivityResponse;
import com.smartagri.advisory.service.FarmActivityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
public class FarmActivityController {
    private final FarmActivityService service;

    public FarmActivityController(FarmActivityService service) {
        this.service = service;
    }

    @PostMapping
    public FarmActivityResponse create(@Valid @RequestBody FarmActivityRequest request) {
        return service.create(request);
    }

    @GetMapping("/field/{fieldId}")
    public List<FarmActivityResponse> byField(@PathVariable Long fieldId) {
        return service.getByFieldId(fieldId);
    }
}
