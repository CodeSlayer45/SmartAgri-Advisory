package com.smartagri.advisory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartagri.advisory.entity.InsuranceClaim;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {
    List<InsuranceClaim> findByFieldIdOrderByFiledAtDesc(Long fieldId);
    List<InsuranceClaim> findAllByOrderByFiledAtDesc();
}