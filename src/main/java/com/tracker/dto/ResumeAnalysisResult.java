package com.tracker.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResumeAnalysisResult {
    private int atsScore;
    private List<String> technicalSkillsIdentified = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private List<String> softSkillsIdentified = new ArrayList<>();
    private List<String> missingSections = new ArrayList<>();
    private List<String> presentSections = new ArrayList<>();
    private List<String> projectSuggestions = new ArrayList<>();
    private List<String> certificationRecommendations = new ArrayList<>();
    private String summarySuggestions;
    private List<String> formattingSuggestions = new ArrayList<>();
    private boolean contactInfoPresent;
    private boolean emailPresent;
    private boolean phonePresent;
    private boolean linkedInOrGithubPresent;
    private int wordCount;
    private Map<String, Integer> scoreBreakdown = new HashMap<>();

    public ResumeAnalysisResult() {}

    public int getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(int atsScore) {
        this.atsScore = atsScore;
    }

    public List<String> getTechnicalSkillsIdentified() {
        return technicalSkillsIdentified;
    }

    public void setTechnicalSkillsIdentified(List<String> technicalSkillsIdentified) {
        this.technicalSkillsIdentified = technicalSkillsIdentified;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public List<String> getSoftSkillsIdentified() {
        return softSkillsIdentified;
    }

    public void setSoftSkillsIdentified(List<String> softSkillsIdentified) {
        this.softSkillsIdentified = softSkillsIdentified;
    }

    public List<String> getMissingSections() {
        return missingSections;
    }

    public void setMissingSections(List<String> missingSections) {
        this.missingSections = missingSections;
    }

    public List<String> getPresentSections() {
        return presentSections;
    }

    public void setPresentSections(List<String> presentSections) {
        this.presentSections = presentSections;
    }

    public List<String> getProjectSuggestions() {
        return projectSuggestions;
    }

    public void setProjectSuggestions(List<String> projectSuggestions) {
        this.projectSuggestions = projectSuggestions;
    }

    public List<String> getCertificationRecommendations() {
        return certificationRecommendations;
    }

    public void setCertificationRecommendations(List<String> certificationRecommendations) {
        this.certificationRecommendations = certificationRecommendations;
    }

    public String getSummarySuggestions() {
        return summarySuggestions;
    }

    public void setSummarySuggestions(String summarySuggestions) {
        this.summarySuggestions = summarySuggestions;
    }

    public List<String> getFormattingSuggestions() {
        return formattingSuggestions;
    }

    public void setFormattingSuggestions(List<String> formattingSuggestions) {
        this.formattingSuggestions = formattingSuggestions;
    }

    public boolean isContactInfoPresent() {
        return contactInfoPresent;
    }

    public void setContactInfoPresent(boolean contactInfoPresent) {
        this.contactInfoPresent = contactInfoPresent;
    }

    public boolean isEmailPresent() {
        return emailPresent;
    }

    public void setEmailPresent(boolean emailPresent) {
        this.emailPresent = emailPresent;
    }

    public boolean isPhonePresent() {
        return phonePresent;
    }

    public void setPhonePresent(boolean phonePresent) {
        this.phonePresent = phonePresent;
    }

    public boolean isLinkedInOrGithubPresent() {
        return linkedInOrGithubPresent;
    }

    public void setLinkedInOrGithubPresent(boolean linkedInOrGithubPresent) {
        this.linkedInOrGithubPresent = linkedInOrGithubPresent;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public Map<String, Integer> getScoreBreakdown() {
        return scoreBreakdown;
    }

    public void setScoreBreakdown(Map<String, Integer> scoreBreakdown) {
        this.scoreBreakdown = scoreBreakdown;
    }
}
