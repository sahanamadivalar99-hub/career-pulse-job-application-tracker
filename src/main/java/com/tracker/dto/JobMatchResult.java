package com.tracker.dto;

import java.util.ArrayList;
import java.util.List;

public class JobMatchResult {
    private int matchPercentage;
    private String matchGrade;
    private List<String> matchingSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private List<String> importantKeywords = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();
    private String jobTitle;
    private String companyName;

    public JobMatchResult() {}

    public int getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(int matchPercentage) {
        this.matchPercentage = matchPercentage;
        if (matchPercentage >= 80) {
            this.matchGrade = "Strong Match";
        } else if (matchPercentage >= 60) {
            this.matchGrade = "Good Match";
        } else if (matchPercentage >= 40) {
            this.matchGrade = "Moderate Match";
        } else {
            this.matchGrade = "Low Match";
        }
    }

    public String getMatchGrade() {
        return matchGrade;
    }

    public void setMatchGrade(String matchGrade) {
        this.matchGrade = matchGrade;
    }

    public List<String> getMatchingSkills() {
        return matchingSkills;
    }

    public void setMatchingSkills(List<String> matchingSkills) {
        this.matchingSkills = matchingSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public List<String> getImportantKeywords() {
        return importantKeywords;
    }

    public void setImportantKeywords(List<String> importantKeywords) {
        this.importantKeywords = importantKeywords;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
