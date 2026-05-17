package com.smartagri.advisory.repository;

import com.smartagri.advisory.entity.AdvisoryAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvisoryAlertRepository extends JpaRepository<AdvisoryAlert, Long> {
    List<AdvisoryAlert> findByFieldIdOrderByCreatedAtDesc(Long fieldId);
}
