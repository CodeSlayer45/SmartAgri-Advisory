package com.smartagri.advisory.repository;

import com.smartagri.advisory.entity.FarmActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FarmActivityRepository extends JpaRepository<FarmActivity, Long> {
    List<FarmActivity> findByFieldId(Long fieldId);
    List<FarmActivity> findByFieldIdAndActivityTypeIgnoreCase(Long fieldId, String activityType);
    List<FarmActivity> findByFieldIdAndActivityDateAfter(Long fieldId, LocalDate activityDate);
}
