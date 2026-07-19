package com.smartagri.advisory.repository;

import com.smartagri.advisory.entity.RecommendationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {
    List<RecommendationHistory> findTop10ByFieldIdOrderByCreatedAtDesc(Long fieldId);

    void deleteByFieldId(Long fieldId);
}
