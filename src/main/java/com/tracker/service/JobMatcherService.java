package com.tracker.service;

import com.tracker.dto.JobMatchResult;
import com.tracker.model.JobMatch;
import com.tracker.model.Resume;
import com.tracker.repository.JobMatchRepository;
import com.tracker.util.AtsKeywordDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class JobMatcherService {

    @Autowired
    private JobMatchRepository jobMatchRepository;

    @Autowired
    private UserService userService;

    public JobMatchResult matchJobDescription(String jobTitle, String companyName, String jobDescription, String resumeText) {
        JobMatchResult result = new JobMatchResult();
        result.setJobTitle(jobTitle != null && !jobTitle.isBlank() ? jobTitle : "Target Job");
        result.setCompanyName(companyName != null && !companyName.isBlank() ? companyName : "Target Company");

        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            result.setMatchPercentage(0);
            result.getRecommendations().add("Please provide a job description to perform matching.");
            return result;
        }

        if (resumeText == null || resumeText.trim().isEmpty()) {
            result.setMatchPercentage(0);
            result.getRecommendations().add("No resume text found. Please upload a resume first.");
            return result;
        }

        String lowerJobDesc = jobDescription.toLowerCase(Locale.ROOT);
        String lowerResume = resumeText.toLowerCase(Locale.ROOT);

        // 1. Identify skills and technologies mentioned in the job description
        List<String> requiredTechSkills = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : AtsKeywordDictionary.TECHNICAL_SKILLS.entrySet()) {
            String skill = entry.getKey();
            for (String alias : entry.getValue()) {
                if (containsWord(lowerJobDesc, alias)) {
                    requiredTechSkills.add(skill);
                    break;
                }
            }
        }

        // 2. Identify soft skills mentioned in the job description
        List<String> requiredSoftSkills = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : AtsKeywordDictionary.SOFT_SKILLS.entrySet()) {
            String softSkill = entry.getKey();
            for (String alias : entry.getValue()) {
                if (containsWord(lowerJobDesc, alias)) {
                    requiredSoftSkills.add(softSkill);
                    break;
                }
            }
        }

        // 3. Extract important keywords from job description
        List<String> importantKeywords = new ArrayList<>();
        importantKeywords.addAll(requiredTechSkills);
        importantKeywords.addAll(requiredSoftSkills);

        // Extract any special patterns in JD (e.g., degree requirements, experience years, etc.)
        if (lowerJobDesc.contains("bachelor") || lowerJobDesc.contains("b.tech") || lowerJobDesc.contains("computer science")) {
            importantKeywords.add("Computer Science Degree");
        }
        if (lowerJobDesc.contains("ci/cd") || lowerJobDesc.contains("jenkins") || lowerJobDesc.contains("pipelines")) {
            if (!importantKeywords.contains("CI/CD")) importantKeywords.add("CI/CD");
        }
        if (lowerJobDesc.contains("agile") || lowerJobDesc.contains("scrum")) {
            if (!importantKeywords.contains("Agile & Scrum")) importantKeywords.add("Agile & Scrum");
        }
        result.setImportantKeywords(importantKeywords);

        // 4. Compare with Resume
        List<String> matchingSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String skill : requiredTechSkills) {
            List<String> aliases = AtsKeywordDictionary.TECHNICAL_SKILLS.getOrDefault(skill, List.of(skill));
            boolean foundInResume = false;
            for (String alias : aliases) {
                if (containsWord(lowerResume, alias)) {
                    foundInResume = true;
                    break;
                }
            }
            if (foundInResume) {
                matchingSkills.add(skill);
            } else {
                missingSkills.add(skill);
            }
        }

        // Check soft skills in resume
        for (String softSkill : requiredSoftSkills) {
            List<String> aliases = AtsKeywordDictionary.SOFT_SKILLS.getOrDefault(softSkill, List.of(softSkill));
            boolean foundInResume = false;
            for (String alias : aliases) {
                if (containsWord(lowerResume, alias)) {
                    foundInResume = true;
                    break;
                }
            }
            if (foundInResume) {
                if (!matchingSkills.contains(softSkill)) matchingSkills.add(softSkill);
            } else {
                if (!missingSkills.contains(softSkill)) missingSkills.add(softSkill);
            }
        }

        result.setMatchingSkills(matchingSkills);
        result.setMissingSkills(missingSkills);

        // 5. Calculate Match Percentage
        int totalRequired = requiredTechSkills.size() + requiredSoftSkills.size();
        int matchPercentage;
        if (totalRequired == 0) {
            // General word overlap calculation if no specific dictionary skills were matched in JD
            matchPercentage = calculateGeneralKeywordOverlap(lowerJobDesc, lowerResume);
        } else {
            int matched = matchingSkills.size();
            matchPercentage = (int) Math.round(((double) matched / totalRequired) * 100);
            // Cap reasonable bounds
            matchPercentage = Math.min(100, Math.max(15, matchPercentage));
        }
        result.setMatchPercentage(matchPercentage);

        // 6. Generate Recommendations based on actual missing skills and job requirements
        List<String> recommendations = new ArrayList<>();
        if (!missingSkills.isEmpty()) {
            recommendations.add("Incorporate missing keywords naturally into your Skills and Project descriptions: " 
                + String.join(", ", missingSkills.subList(0, Math.min(4, missingSkills.size()))) + ".");
        }
        if (missingSkills.contains("Spring Boot") || missingSkills.contains("Microservices")) {
            recommendations.add("Highlight any REST API or backend services built using Spring Boot framework.");
        }
        if (missingSkills.contains("Docker") || missingSkills.contains("CI/CD")) {
            recommendations.add("Mention containerization (Docker) and deployment pipelines in your work or academic projects.");
        }
        if (matchPercentage < 60) {
            recommendations.add("Customize your Professional Summary to mirror the role title '" 
                + result.getJobTitle() + "' and target core requirements specified in the job posting.");
        }
        recommendations.add("Include quantitative metrics demonstrating the real-world business impact of your listed technologies.");
        result.setRecommendations(recommendations);

        return result;
    }

    @Transactional
    public JobMatch saveJobMatch(JobMatchResult matchResult, String jobDescription, Resume resume) {
        JobMatch entity = new JobMatch();
        entity.setJobTitle(matchResult.getJobTitle());
        entity.setCompanyName(matchResult.getCompanyName());
        entity.setJobDescription(jobDescription);
        entity.setResume(resume);
        entity.setUser(resume != null && resume.getUser() != null ? resume.getUser() : userService.getCurrentUser());
        entity.setMatchPercentage(matchResult.getMatchPercentage());
        entity.setMatchingSkills(String.join(", ", matchResult.getMatchingSkills()));
        entity.setMissingSkills(String.join(", ", matchResult.getMissingSkills()));
        entity.setImportantKeywords(String.join(", ", matchResult.getImportantKeywords()));
        entity.setRecommendations(String.join("||", matchResult.getRecommendations()));
        return jobMatchRepository.save(entity);
    }

    public List<JobMatch> getRecentMatches() {
        return jobMatchRepository.findAllByUserOrderByMatchedAtDesc(userService.getCurrentUser());
    }

    private int calculateGeneralKeywordOverlap(String jobDesc, String resumeText) {
        Set<String> jdWords = extractMeaningfulWords(jobDesc);
        Set<String> resumeWords = extractMeaningfulWords(resumeText);
        if (jdWords.isEmpty()) return 50;

        long overlap = jdWords.stream().filter(resumeWords::contains).count();
        int pct = (int) Math.round(((double) overlap / jdWords.size()) * 100);
        return Math.min(95, Math.max(25, pct));
    }

    private Set<String> extractMeaningfulWords(String text) {
        Set<String> stopWords = Set.of(
            "the", "and", "a", "an", "in", "to", "of", "for", "with", "is", "are", "on", "at", "by", "from",
            "this", "that", "it", "as", "be", "or", "will", "you", "your", "we", "our", "all", "have", "has"
        );
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-zA-Z0-9]+"))
                .filter(w -> w.length() > 3 && !stopWords.contains(w))
                .collect(Collectors.toSet());
    }

    private boolean containsWord(String text, String target) {
        String patternString = "\\b" + Pattern.quote(target.toLowerCase(Locale.ROOT)) + "\\b";
        return Pattern.compile(patternString).matcher(text).find();
    }
}
