package com.tracker.repository;

import com.tracker.model.ResumeAnalysis;
import com.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    Optional<ResumeAnalysis> findByResumeId(Long resumeId);
    Optional<ResumeAnalysis> findTopByOrderByAnalyzedAtDesc();
    Optional<ResumeAnalysis> findTopByResumeUserOrderByAnalyzedAtDesc(User user);
}
