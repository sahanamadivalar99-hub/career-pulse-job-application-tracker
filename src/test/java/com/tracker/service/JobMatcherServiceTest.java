package com.tracker.service;

import com.tracker.dto.JobMatchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JobMatcherServiceTest {

    @Autowired
    private JobMatcherService jobMatcherService;

    @Test
    public void testJobMatching() {
        String jobDescription =
            "We are seeking a Backend Engineer with strong expertise in Java, Spring Boot, MySQL, REST APIs, and Docker.";

        String resumeText =
            "Skills: Java, SQL, MySQL, Git, HTML, CSS. Built enterprise backend services with relational databases.";

        JobMatchResult result = jobMatcherService.matchJobDescription(
            "Backend Engineer", "Tech Corp", jobDescription, resumeText
        );

        assertNotNull(result);
        assertTrue(result.getMatchPercentage() > 0, "Match percentage should be greater than zero");
        assertTrue(result.getMatchingSkills().contains("Java"), "Should match Java");
        assertTrue(result.getMissingSkills().contains("Spring Boot") || result.getMissingSkills().contains("Docker"), 
                   "Should identify Spring Boot or Docker as missing");
        assertFalse(result.getRecommendations().isEmpty(), "Should give recommendations");
    }
}
