package com.tracker.service;

import com.tracker.dto.ResumeAnalysisResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ResumeAnalyzerServiceTest {

    @Autowired
    private ResumeAnalyzerService analyzerService;

    @Test
    public void testResumeAnalysis_WithJavaFullStackText() {
        String sampleResume =
            "SAHANA DEVELOPER\n" +
            "Email: sahana@example.com | Phone: 555-123-4567\n" +
            "GitHub: github.com/sahanadev | LinkedIn: linkedin.com/in/sahanadev\n\n" +
            "PROFESSIONAL SUMMARY\n" +
            "Passionate Java Developer with 2 years of experience in backend development.\n\n" +
            "EDUCATION\n" +
            "B.S. in Computer Science - 2024\n\n" +
            "TECHNICAL SKILLS\n" +
            "Java, SQL, HTML, CSS, JavaScript, Git, MySQL\n\n" +
            "PROJECTS\n" +
            "1. E-Commerce Backend\n" +
            "- Built REST APIs using Java and MySQL, reducing latency by 35%.\n" +
            "- Collaborated with cross-functional team in Agile sprints.\n";

        ResumeAnalysisResult result = analyzerService.analyzeResumeText(sampleResume);

        assertNotNull(result);
        assertTrue(result.getAtsScore() > 50, "ATS score should be greater than 50 for a detailed resume");
        assertTrue(result.getTechnicalSkillsIdentified().contains("Java"), "Should detect Java");
        assertTrue(result.getTechnicalSkillsIdentified().contains("SQL"), "Should detect SQL");
        assertTrue(result.isContactInfoPresent(), "Should detect contact information");
        assertTrue(result.isEmailPresent(), "Should detect email");
        assertTrue(result.isPhonePresent(), "Should detect phone");
        assertTrue(result.isLinkedInOrGithubPresent(), "Should detect GitHub / LinkedIn");
        assertFalse(result.getMissingSkills().isEmpty(), "Should provide missing skills to improve");
        assertFalse(result.getProjectSuggestions().isEmpty(), "Should provide project suggestions");
    }

    @Test
    public void testResumeAnalysis_WithEmptyText() {
        ResumeAnalysisResult result = analyzerService.analyzeResumeText("");
        assertNotNull(result);
        assertEquals(0, result.getAtsScore());
        assertFalse(result.getFormattingSuggestions().isEmpty());
    }
}
