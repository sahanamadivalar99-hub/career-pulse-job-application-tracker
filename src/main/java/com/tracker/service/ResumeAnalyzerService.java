package com.tracker.service;

import com.tracker.dto.ResumeAnalysisResult;
import com.tracker.model.Resume;
import com.tracker.model.ResumeAnalysis;
import com.tracker.repository.ResumeAnalysisRepository;
import com.tracker.repository.ResumeRepository;
import com.tracker.util.AtsKeywordDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ResumeAnalyzerService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeAnalysisRepository analysisRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}|\\b\\d{10}\\b"
    );

    private static final Pattern LINKEDIN_PATTERN = Pattern.compile(
        "linkedin\\.com/[a-zA-Z0-9_-]+",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern GITHUB_PATTERN = Pattern.compile(
        "github\\.com/[a-zA-Z0-9_-]+",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern NUMERICAL_METRIC_PATTERN = Pattern.compile(
        "\\b(\\d+%(?:\\s*increase|\\s*decrease|\\s*growth|\\s*reduction)?|\\$\\d+[kKmM]?|\\d+\\+?\\s*(?:users|clients|requests|transactions|projects|ms|seconds|minutes|hours|days))\\b",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Performs comprehensive ATS analysis on extracted resume text.
     */
    public ResumeAnalysisResult analyzeResumeText(String resumeText) {
        ResumeAnalysisResult result = new ResumeAnalysisResult();

        if (resumeText == null || resumeText.trim().isEmpty()) {
            result.setAtsScore(0);
            result.getMissingSections().addAll(AtsKeywordDictionary.RESUME_SECTIONS.keySet());
            result.getFormattingSuggestions().add("The uploaded resume appears to be empty or unscannable.");
            return result;
        }

        String lowerText = resumeText.toLowerCase(Locale.ROOT);
        String[] words = resumeText.trim().split("\\s+");
        result.setWordCount(words.length);

        // 1. Contact Information Detection
        boolean hasEmail = EMAIL_PATTERN.matcher(resumeText).find();
        boolean hasPhone = PHONE_PATTERN.matcher(resumeText).find();
        boolean hasLinkedInOrGithub = LINKEDIN_PATTERN.matcher(resumeText).find() || GITHUB_PATTERN.matcher(resumeText).find();
        boolean contactPresent = hasEmail || hasPhone;

        result.setEmailPresent(hasEmail);
        result.setPhonePresent(hasPhone);
        result.setLinkedInOrGithubPresent(hasLinkedInOrGithub);
        result.setContactInfoPresent(contactPresent);

        // 2. Resume Sections Detection
        List<String> presentSections = new ArrayList<>();
        List<String> missingSections = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : AtsKeywordDictionary.RESUME_SECTIONS.entrySet()) {
            String sectionName = entry.getKey();
            List<String> keywords = entry.getValue();
            boolean found = false;

            if ("Contact Information".equalsIgnoreCase(sectionName)) {
                found = contactPresent;
            } else {
                for (String kw : keywords) {
                    if (containsWord(lowerText, kw)) {
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                presentSections.add(sectionName);
            } else {
                missingSections.add(sectionName);
            }
        }
        result.setPresentSections(presentSections);
        result.setMissingSections(missingSections);

        // 3. Technical Skills Extraction
        List<String> detectedTechSkills = new ArrayList<>();
        List<String> missingTechSkills = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : AtsKeywordDictionary.TECHNICAL_SKILLS.entrySet()) {
            String skillName = entry.getKey();
            List<String> aliases = entry.getValue();
            boolean found = false;
            for (String alias : aliases) {
                if (containsWord(lowerText, alias)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                detectedTechSkills.add(skillName);
            } else {
                missingTechSkills.add(skillName);
            }
        }
        result.setTechnicalSkillsIdentified(detectedTechSkills);

        // Curate the most critical missing technical skills (top 6-8 recommended based on what is detected)
        List<String> curatedMissingSkills = new ArrayList<>();
        if (detectedTechSkills.contains("Java") && !detectedTechSkills.contains("Spring Boot")) {
            curatedMissingSkills.add("Spring Boot");
        }
        if (!detectedTechSkills.contains("REST APIs")) {
            curatedMissingSkills.add("REST APIs");
        }
        if (!detectedTechSkills.contains("Docker")) {
            curatedMissingSkills.add("Docker & Containers");
        }
        if (!detectedTechSkills.contains("Git & GitHub")) {
            curatedMissingSkills.add("Git & GitHub");
        }
        if (!detectedTechSkills.contains("Microservices")) {
            curatedMissingSkills.add("Microservices");
        }
        if (!detectedTechSkills.contains("CI/CD")) {
            curatedMissingSkills.add("CI/CD Pipelines");
        }
        if (!detectedTechSkills.contains("JUnit / Mockito")) {
            curatedMissingSkills.add("JUnit / Unit Testing");
        }
        if (curatedMissingSkills.size() < 5) {
            for (String s : missingTechSkills) {
                if (!curatedMissingSkills.contains(s) && curatedMissingSkills.size() < 6) {
                    curatedMissingSkills.add(s);
                }
            }
        }
        result.setMissingSkills(curatedMissingSkills);

        // 4. Soft Skills Extraction
        List<String> detectedSoftSkills = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : AtsKeywordDictionary.SOFT_SKILLS.entrySet()) {
            String softSkill = entry.getKey();
            List<String> aliases = entry.getValue();
            for (String alias : aliases) {
                if (containsWord(lowerText, alias)) {
                    detectedSoftSkills.add(softSkill);
                    break;
                }
            }
        }
        result.setSoftSkillsIdentified(detectedSoftSkills);

        // 5. Action Verbs & Metrics
        int actionVerbCount = 0;
        for (String verb : AtsKeywordDictionary.ACTION_VERBS) {
            if (containsWord(lowerText, verb)) {
                actionVerbCount++;
            }
        }

        Matcher metricMatcher = NUMERICAL_METRIC_PATTERN.matcher(resumeText);
        int metricCount = 0;
        while (metricMatcher.find()) {
            metricCount++;
        }

        // 6. Transparent ATS Score Calculation (0 - 100)
        // A) Section Completeness (Max 25 pts)
        int sectionScore = 0;
        if (contactPresent) sectionScore += 5;
        if (presentSections.contains("Education")) sectionScore += 4;
        if (presentSections.contains("Technical Skills")) sectionScore += 5;
        if (presentSections.contains("Work Experience")) sectionScore += 4;
        if (presentSections.contains("Projects")) sectionScore += 4;
        if (presentSections.contains("Professional Summary")) sectionScore += 3;
        sectionScore = Math.min(25, sectionScore);

        // B) Technical Skills Breadth (Max 30 pts)
        int techScore = Math.min(30, detectedTechSkills.size() * 3);

        // C) Soft Skills & Collaboration (Max 15 pts)
        int softScore = Math.min(15, detectedSoftSkills.size() * 3);

        // D) Action Verbs & Measurable Metrics (Max 15 pts)
        int actionScore = Math.min(8, actionVerbCount * 2);
        int metricScore = Math.min(7, metricCount * 2);
        int impactScore = actionScore + metricScore;

        // E) Format & Presentation (Max 15 pts)
        int formatScore = 0;
        if (words.length >= 250 && words.length <= 900) formatScore += 5; // Ideal 1-2 page length
        else if (words.length > 900 && words.length <= 1500) formatScore += 3;
        else formatScore += 1;

        if (hasEmail && hasPhone) formatScore += 5;
        if (hasLinkedInOrGithub) formatScore += 5;

        int finalScore = sectionScore + techScore + softScore + impactScore + formatScore;
        finalScore = Math.min(100, Math.max(10, finalScore));

        result.setAtsScore(finalScore);
        result.getScoreBreakdown().put("Sections Completeness", sectionScore);
        result.getScoreBreakdown().put("Technical Skills", techScore);
        result.getScoreBreakdown().put("Soft Skills", softScore);
        result.getScoreBreakdown().put("Action & Metrics", impactScore);
        result.getScoreBreakdown().put("Format & Details", formatScore);

        // 7. Project Improvement Suggestions
        List<String> projectSuggestions = new ArrayList<>();
        projectSuggestions.add("Add 2 practical Java/Spring Boot projects highlighting REST APIs and database persistence (e.g. E-Commerce Backend or Job Portal).");
        projectSuggestions.add("Include measurable outcomes in project bullet points (e.g., 'Reduced response latency by 35%' or 'Handled 500+ requests/sec').");
        if (!detectedTechSkills.contains("Docker")) {
            projectSuggestions.add("Containerize your application with a Dockerfile and docker-compose to demonstrate production readiness.");
        }
        if (!hasLinkedInOrGithub) {
            projectSuggestions.add("Add clickable GitHub repository links and live demonstration URLs for your top projects.");
        }
        result.setProjectSuggestions(projectSuggestions);

        // 8. Certification Recommendations
        List<String> certifications = new ArrayList<>();
        if (detectedTechSkills.contains("Java")) {
            certifications.addAll(AtsKeywordDictionary.CERTIFICATIONS_BY_DOMAIN.get("Java & Backend"));
        } else {
            certifications.addAll(AtsKeywordDictionary.CERTIFICATIONS_BY_DOMAIN.get("Cloud & DevOps"));
        }
        result.setCertificationRecommendations(certifications.subList(0, Math.min(3, certifications.size())));

        // 9. Professional Summary Suggestions
        if (!presentSections.contains("Professional Summary")) {
            result.setSummarySuggestions("Add a targeted 3-4 line Professional Summary at the top: 'Dedicated Java Full-Stack Developer with experience building scalable RESTful microservices, Spring Boot applications, and relational databases. Passionate about writing clean, maintainable code and solving complex engineering challenges.'");
        } else {
            result.setSummarySuggestions("Ensure your summary highlights your core strengths, years of hands-on project experience, and 3-4 primary technologies (e.g., Java 21, Spring Boot, MySQL, REST APIs) rather than generic adjectives.");
        }

        // 10. Formatting and Readability Suggestions
        List<String> formatting = new ArrayList<>();
        if (!hasLinkedInOrGithub) {
            formatting.add("Include active links to your GitHub profile and LinkedIn page in the header.");
        }
        if (metricCount < 2) {
            formatting.add("Quantify your accomplishments. Replace statements like 'Worked on database queries' with 'Optimized MySQL queries to decrease execution time by 40%'.");
        }
        if (words.length < 250) {
            formatting.add("Your resume seems short (" + words.length + " words). Expand upon key academic/personal projects and technical responsibilities.");
        } else if (words.length > 900) {
            formatting.add("Your resume is relatively long (" + words.length + " words). Strive for a concise 1-2 page format with crisp bullet points.");
        }
        if (missingSections.size() > 0) {
            formatting.add("Add standard headers for missing sections: " + String.join(", ", missingSections) + ".");
        }
        formatting.add("Use standard bullet points with strong action verbs (Built, Architected, Automated, Implemented) at the start of each line.");
        result.setFormattingSuggestions(formatting);

        return result;
    }

    /**
     * Persists analysis result into MySQL database
     */
    @Transactional
    public ResumeAnalysis saveAnalysis(Resume resume, ResumeAnalysisResult result) {
        ResumeAnalysis entity = analysisRepository.findByResumeId(resume.getId())
                .orElse(new ResumeAnalysis());

        entity.setResume(resume);
        entity.setAtsScore(result.getAtsScore());
        entity.setTechnicalSkills(String.join(", ", result.getTechnicalSkillsIdentified()));
        entity.setMissingSkills(String.join(", ", result.getMissingSkills()));
        entity.setSoftSkills(String.join(", ", result.getSoftSkillsIdentified()));
        entity.setMissingSections(String.join(", ", result.getMissingSections()));
        entity.setProjectSuggestions(String.join("||", result.getProjectSuggestions()));
        entity.setCertificationRecommendations(String.join("||", result.getCertificationRecommendations()));
        entity.setSummarySuggestions(result.getSummarySuggestions());
        entity.setFormattingSuggestions(String.join("||", result.getFormattingSuggestions()));
        entity.setWordCount(result.getWordCount());
        entity.setContactInfoPresent(result.isContactInfoPresent());

        return analysisRepository.save(entity);
    }

    /**
     * Converts a persisted ResumeAnalysis entity back to DTO
     */
    public ResumeAnalysisResult toDto(ResumeAnalysis entity) {
        ResumeAnalysisResult dto = new ResumeAnalysisResult();
        dto.setAtsScore(entity.getAtsScore());
        dto.setWordCount(entity.getWordCount());
        dto.setContactInfoPresent(entity.isContactInfoPresent());
        dto.setSummarySuggestions(entity.getSummarySuggestions());

        if (entity.getTechnicalSkills() != null && !entity.getTechnicalSkills().isEmpty()) {
            dto.setTechnicalSkillsIdentified(Arrays.asList(entity.getTechnicalSkills().split(", ")));
        }
        if (entity.getMissingSkills() != null && !entity.getMissingSkills().isEmpty()) {
            dto.setMissingSkills(Arrays.asList(entity.getMissingSkills().split(", ")));
        }
        if (entity.getSoftSkills() != null && !entity.getSoftSkills().isEmpty()) {
            dto.setSoftSkillsIdentified(Arrays.asList(entity.getSoftSkills().split(", ")));
        }
        if (entity.getMissingSections() != null && !entity.getMissingSections().isEmpty()) {
            dto.setMissingSections(Arrays.asList(entity.getMissingSections().split(", ")));
        }
        if (entity.getProjectSuggestions() != null && !entity.getProjectSuggestions().isEmpty()) {
            dto.setProjectSuggestions(Arrays.asList(entity.getProjectSuggestions().split("\\|\\|")));
        }
        if (entity.getCertificationRecommendations() != null && !entity.getCertificationRecommendations().isEmpty()) {
            dto.setCertificationRecommendations(Arrays.asList(entity.getCertificationRecommendations().split("\\|\\|")));
        }
        if (entity.getFormattingSuggestions() != null && !entity.getFormattingSuggestions().isEmpty()) {
            dto.setFormattingSuggestions(Arrays.asList(entity.getFormattingSuggestions().split("\\|\\|")));
        }

        return dto;
    }

    private boolean containsWord(String text, String target) {
        // Escapes target and checks with boundary or substring
        String patternString = "\\b" + Pattern.quote(target.toLowerCase(Locale.ROOT)) + "\\b";
        return Pattern.compile(patternString).matcher(text).find();
    }
}
