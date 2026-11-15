package com.tracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_analysis")
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "ats_score", nullable = false)
    private int atsScore;

    @Column(name = "technical_skills", columnDefinition = "TEXT")
    private String technicalSkills; // comma or JSON separated

    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills;

    @Column(name = "soft_skills", columnDefinition = "TEXT")
    private String softSkills;

    @Column(name = "missing_sections", columnDefinition = "TEXT")
    private String missingSections;

    @Column(name = "project_suggestions", columnDefinition = "TEXT")
    private String projectSuggestions;

    @Column(name = "certification_recommendations", columnDefinition = "TEXT")
    private String certificationRecommendations;

    @Column(name = "summary_suggestions", columnDefinition = "TEXT")
    private String summarySuggestions;

    @Column(name = "formatting_suggestions", columnDefinition = "TEXT")
    private String formattingSuggestions;

    @Column(name = "word_count")
    private int wordCount;

    @Column(name = "contact_info_present")
    private boolean contactInfoPresent;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt = LocalDateTime.now();

    public ResumeAnalysis() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    public int getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(int atsScore) {
        this.atsScore = atsScore;
    }

    public String getTechnicalSkills() {
        return technicalSkills;
    }

    public void setTechnicalSkills(String technicalSkills) {
        this.technicalSkills = technicalSkills;
    }

    public String getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(String missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getSoftSkills() {
        return softSkills;
    }

    public void setSoftSkills(String softSkills) {
        this.softSkills = softSkills;
    }

    public String getMissingSections() {
        return missingSections;
    }

    public void setMissingSections(String missingSections) {
        this.missingSections = missingSections;
    }

    public String getProjectSuggestions() {
        return projectSuggestions;
    }

    public void setProjectSuggestions(String projectSuggestions) {
        this.projectSuggestions = projectSuggestions;
    }

    public String getCertificationRecommendations() {
        return certificationRecommendations;
    }

    public void setCertificationRecommendations(String certificationRecommendations) {
        this.certificationRecommendations = certificationRecommendations;
    }

    public String getSummarySuggestions() {
        return summarySuggestions;
    }

    public void setSummarySuggestions(String summarySuggestions) {
        this.summarySuggestions = summarySuggestions;
    }

    public String getFormattingSuggestions() {
        return formattingSuggestions;
    }

    public void setFormattingSuggestions(String formattingSuggestions) {
        this.formattingSuggestions = formattingSuggestions;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public boolean isContactInfoPresent() {
        return contactInfoPresent;
    }

    public void setContactInfoPresent(boolean contactInfoPresent) {
        this.contactInfoPresent = contactInfoPresent;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}
