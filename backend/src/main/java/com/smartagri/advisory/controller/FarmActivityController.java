package com.smartagri.advisory.controller;

import com.smartagri.advisory.dto.FarmActivityRequest;
import com.smartagri.advisory.dto.FarmActivityResponse;
import com.smartagri.advisory.dto.FarmActivityUpdateRequest;
import com.smartagri.advisory.service.FarmActivityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    @PutMapping("/{activityId}")
    public FarmActivityResponse update(@PathVariable Long activityId,
                                       @Valid @RequestBody FarmActivityUpdateRequest request) {
        return service.update(activityId, request);
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> delete(@PathVariable Long activityId) {
        service.deleteById(activityId);
        return ResponseEntity.noContent().build();
    }
}
