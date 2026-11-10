package com.tracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.dto.ChatMessageDto;
import com.tracker.dto.DashboardStatsDto;
import com.tracker.dto.JobMatchResult;
import com.tracker.dto.ResumeAnalysisResult;
import com.tracker.model.*;
import com.tracker.repository.*;
import com.tracker.util.PdfTextExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "sahana.bengaluru@example.com")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CompleteModulesAuditTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobApplicationService applicationService;

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ResumeParserService resumeParserService;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeAnalyzerService analyzerService;

    @Autowired
    private JobMatcherService matcherService;

    @Autowired
    private CareerAssistantService assistantService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------
    // FEATURE 1: Dashboard and application statistics
    // -------------------------------------------------------------
    @Test
    @Order(1)
    public void testFeature1_DashboardAndStatistics() throws Exception {
        DashboardStatsDto stats = applicationService.getDashboardStatistics();
        assertNotNull(stats, "Stats should not be null");
        assertTrue(stats.getTotalApplications() > 0, "Preloaded total applications should be > 0");

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attributeExists("recentApplications"))
                .andExpect(model().attributeExists("upcomingInterviews"))
                .andExpect(model().attributeExists("upcomingReminders"));
    }

    // -------------------------------------------------------------
    // FEATURE 2: Add, edit, and delete job applications
    // -------------------------------------------------------------
    @Test
    @Order(2)
    public void testFeature2_JobApplicationCrud() throws Exception {
        // Create new application
        JobApplication newApp = new JobApplication("Airbnb", "Senior Backend Engineer", "San Francisco, CA", ApplicationStatus.APPLIED, LocalDate.now());
        newApp.setSalaryRange("$160,000 - $190,000");
        newApp.setJobLink("https://careers.airbnb.com/positions/123");
        newApp.setNotes("Referral by team lead");
        JobApplication saved = applicationService.saveApplication(newApp);

        assertNotNull(saved.getId(), "Saved application must have an ID");
        assertEquals("Airbnb", saved.getCompanyName());

        // Verify it exists in database
        Optional<JobApplication> fetched = applicationRepository.findById(saved.getId());
        assertTrue(fetched.isPresent(), "Application must be queryable from repository");

        // Edit application
        saved.setJobRole("Lead Java Engineer");
        saved.setStatus(ApplicationStatus.IN_REVIEW);
        JobApplication updated = applicationService.saveApplication(saved);
        assertEquals("Lead Java Engineer", updated.getJobRole());
        assertEquals(ApplicationStatus.IN_REVIEW, updated.getStatus());

        // Verify detail endpoint
        mockMvc.perform(get("/applications/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("applications/detail"))
                .andExpect(model().attributeExists("app"));

        // Delete application
        applicationService.deleteApplication(saved.getId());
        assertTrue(applicationRepository.findById(saved.getId()).isEmpty(), "Application should be deleted from DB");
    }

    // -------------------------------------------------------------
    // FEATURE 3: Application status management & Kanban
    // -------------------------------------------------------------
    @Test
    @Order(3)
    public void testFeature3_StatusManagementAndKanban() throws Exception {
        // Kanban view loads
        mockMvc.perform(get("/applications/kanban"))
                .andExpect(status().isOk())
                .andExpect(view().name("applications/kanban"))
                .andExpect(model().attributeExists("groupedApplications"));

        // Create an app to test status progression
        JobApplication app = new JobApplication("Stripe", "Payment Platform Engineer", "Remote", ApplicationStatus.APPLIED, LocalDate.now());
        app = applicationService.saveApplication(app);

        // Update status to INTERVIEW via AJAX
        mockMvc.perform(post("/applications/api/status")
                .param("id", String.valueOf(app.getId()))
                .param("status", "INTERVIEW")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.newStatus").value("INTERVIEW"));

        JobApplication reloaded = applicationRepository.findById(app.getId()).orElseThrow();
        assertEquals(ApplicationStatus.INTERVIEW, reloaded.getStatus());

        // Update status to OFFER
        applicationService.updateStatus(app.getId(), ApplicationStatus.OFFER);
        assertEquals(ApplicationStatus.OFFER, applicationRepository.findById(app.getId()).orElseThrow().getStatus());

        // Clean up
        applicationService.deleteApplication(app.getId());
    }

    // -------------------------------------------------------------
    // FEATURES 4, 5, 6, 7, 8, 9: Resume PDF Upload, PDFBox Text Extraction,
    // Transparent ATS Scoring, Skill Detection, Missing Skills, and Suggestions
    // (TESTED ON DYNAMIC UNSEEN RESUMES - NOT HARDFACED)
    // -------------------------------------------------------------
    @Test
    @Order(4)
    public void testFeature4To9_ResumePdfUploadAndDynamicAnalysis() throws Exception {
        // Create an authentic, brand new PDF resume dynamically
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                cs.setLeading(16.0f);
                cs.newLineAtOffset(50, 720);

                cs.showText("JORDAN SMITH - CLOUD & PYTHON DEVELOPER");
                cs.newLine();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.showText("Email: jordan.smith@example.com | Phone: 408-555-0199");
                cs.newLine();
                cs.showText("GitHub: github.com/jordan-cloud | LinkedIn: linkedin.com/in/jordan-smith");
                cs.newLine();
                cs.newLine();

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.showText("PROFESSIONAL SUMMARY");
                cs.newLine();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.showText("DevOps and Python engineer with 4 years building serverless pipelines on AWS and Docker.");
                cs.newLine();
                cs.newLine();

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.showText("EDUCATION");
                cs.newLine();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.showText("B.S. in Computer Engineering, San Jose State University, 2022");
                cs.newLine();
                cs.newLine();

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.showText("TECHNICAL SKILLS");
                cs.newLine();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.showText("Python, AWS, Docker, Kubernetes, Linux, PostgreSQL, Git, Redis, CI/CD");
                cs.newLine();
                cs.newLine();

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.showText("WORK EXPERIENCE & PROJECTS");
                cs.newLine();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.showText("Automated CI/CD deployments reducing release time by 45%.");
                cs.newLine();
                cs.showText("Architected microservices handling 1500 requests per minute with Redis caching.");
                cs.newLine();
                cs.endText();
            }
            doc.save(baos);
        }

        MockMultipartFile pdfFile = new MockMultipartFile(
            "resumeFile", "Jordan_Smith_Cloud_Resume.pdf", "application/pdf", baos.toByteArray()
        );

        // Upload and execute full PDFBox extraction + analysis
        Resume uploadedResume = resumeParserService.uploadAndAnalyze(pdfFile);
        assertNotNull(uploadedResume.getId());
        assertNotNull(uploadedResume.getAnalysis());

        // Verify Apache PDFBox extracted the actual text
        String text = uploadedResume.getExtractedText();
        assertTrue(text.contains("JORDAN SMITH"), "Extracted text must have applicant name");
        assertTrue(text.contains("Python"), "Extracted text must contain Python");
        assertTrue(text.contains("Kubernetes"), "Extracted text must contain Kubernetes");

        // Verify Skills Identified dynamically
        ResumeAnalysisResult analysis = analyzerService.toDto(uploadedResume.getAnalysis());
        assertTrue(analysis.getTechnicalSkillsIdentified().contains("Python"));
        assertTrue(analysis.getTechnicalSkillsIdentified().contains("AWS"));
        assertTrue(analysis.getTechnicalSkillsIdentified().contains("Docker"));
        assertTrue(analysis.getTechnicalSkillsIdentified().contains("Kubernetes"));

        // Verify Missing Skills (Java, Spring Boot, etc. are NOT in this Python resume!)
        assertTrue(analysis.getMissingSkills().contains("Spring Boot") || analysis.getMissingSkills().contains("REST APIs"),
                   "Should identify missing enterprise skills");

        // Verify Transparent ATS Score
        assertTrue(analysis.getAtsScore() >= 60, "Complete resume should get an ATS score >= 60");
        assertTrue(analysis.getAtsScore() <= 100);

        // Verify Suggestions
        assertFalse(analysis.getProjectSuggestions().isEmpty());
        assertFalse(analysis.getCertificationRecommendations().isEmpty());

        // Test web view for this specific resume ID
        mockMvc.perform(get("/resume-analyzer").param("id", String.valueOf(uploadedResume.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("resume/analyzer"))
                .andExpect(model().attributeExists("analysis"))
                .andExpect(model().attributeExists("currentResume"));
    }

    // -------------------------------------------------------------
    // FEATURE 10: Job description matching
    // -------------------------------------------------------------
    @Test
    @Order(5)
    public void testFeature10_JobDescriptionMatching() throws Exception {
        String jobDesc = "Seeking a Senior Cloud Platform Engineer with deep expertise in Python, AWS, Docker, Kubernetes, and PostgreSQL.";
        String resumeText = "Experienced with Python, AWS, and Linux. Built container solutions.";

        JobMatchResult match = matcherService.matchJobDescription("Senior Cloud Engineer", "FinTech Inc", jobDesc, resumeText);
        assertNotNull(match);
        assertTrue(match.getMatchPercentage() > 0);
        assertTrue(match.getMatchingSkills().contains("Python"));
        assertTrue(match.getMatchingSkills().contains("AWS"));
        assertTrue(match.getMissingSkills().contains("Docker") || match.getMissingSkills().contains("Kubernetes"));
        assertFalse(match.getRecommendations().isEmpty());

        // Verify Job Matcher Controller
        mockMvc.perform(get("/job-matcher"))
                .andExpect(status().isOk())
                .andExpect(view().name("matcher/index"))
                .andExpect(model().attributeExists("resumes"));
    }

    // -------------------------------------------------------------
    // FEATURE 11: AI Career Assistant with Rule-Based Fallback
    // -------------------------------------------------------------
    @Test
    @Order(6)
    public void testFeature11_CareerAssistant() throws Exception {
        // Query the assistant
        ChatMessageDto reply = assistantService.processUserMessage("What skills should I learn for Java Developer jobs?");
        assertNotNull(reply);
        assertEquals("assistant", reply.getRole());
        assertNotNull(reply.getMessage());
        assertTrue(reply.getMessage().contains("Java") || reply.getMessage().contains("Spring"));
        assertEquals("RULE_BASED", reply.getProvider(), "Without external LLM API key, provider must be transparently RULE_BASED");

        // Test API Endpoint with JSON
        mockMvc.perform(post("/career-assistant/api/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "How can I improve my resume?")))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.provider").value("RULE_BASED"));
    }

    // -------------------------------------------------------------
    // FEATURE 12: Interview reminders & follow-ups
    // -------------------------------------------------------------
    @Test
    @Order(7)
    public void testFeature12_InterviewReminders() throws Exception {
        Reminder rem = new Reminder("Technical Interview with Oracle", ReminderType.INTERVIEW, LocalDate.now().plusDays(4), LocalTime.of(15, 30), "Review collections and concurrency");
        Reminder saved = reminderService.saveReminder(rem);
        assertNotNull(saved.getId());
        assertFalse(saved.isCompleted());

        // Toggle completion
        Reminder toggled = reminderService.toggleCompletion(saved.getId());
        assertTrue(toggled.isCompleted(), "Reminder should be marked completed");

        // View page
        mockMvc.perform(get("/reminders"))
                .andExpect(status().isOk())
                .andExpect(view().name("reminders/list"))
                .andExpect(model().attributeExists("reminders"));

        // Delete
        reminderService.deleteReminder(saved.getId());
        assertTrue(reminderRepository.findById(saved.getId()).isEmpty());
    }

    // -------------------------------------------------------------
    // FEATURE 13: Database persistence & Settings
    // -------------------------------------------------------------
    @Test
    @Order(8)
    public void testFeature13_PersistenceAndSettings() throws Exception {
        // Test settings update
        User user = userService.updateSettings("Sahana K.", "sahana.k@example.com", "Senior Java Engineer", "");
        assertEquals("Sahana K.", user.getFullName());
        assertEquals("Senior Java Engineer", user.getTargetRole());

        // Test companies persistence
        Company company = companyService.getOrCreateCompany("Netflix", "Los Gatos, CA");
        assertNotNull(company.getId());
        assertEquals("Netflix", company.getName());

        // Test company search
        assertFalse(companyService.searchCompanies("Netflix").isEmpty());
    }

    // -------------------------------------------------------------
    // FEATURE 14: Authentication, Registration & Multi-User Data Isolation
    // -------------------------------------------------------------
    @Test
    @Order(9)
    public void testFeature14_AuthenticationAndDataIsolation() throws Exception {
        // 1. User Registration with Validation
        com.tracker.dto.UserRegistrationDto dto = new com.tracker.dto.UserRegistrationDto();
        dto.setFullName("Priya Sharma");
        dto.setEmail("priya.sharma@example.com");
        dto.setPassword("Secret@123");
        dto.setConfirmPassword("Secret@123");
        dto.setLocation("Bengaluru, Karnataka, India");
        dto.setTargetRole("Backend Engineer");

        User registeredUser = userService.registerUser(dto);
        assertNotNull(registeredUser.getId());
        assertEquals("priya.sharma@example.com", registeredUser.getEmail());
        assertTrue(registeredUser.getPassword().startsWith("$2a$") || registeredUser.getPassword().startsWith("$2b$"), "Password must be BCrypt hashed");

        // Duplicate registration must fail
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));

        // Mismatched passwords must fail
        com.tracker.dto.UserRegistrationDto badDto = new com.tracker.dto.UserRegistrationDto();
        badDto.setFullName("Test User");
        badDto.setEmail("test.mismatch@example.com");
        badDto.setPassword("Pass1234");
        badDto.setConfirmPassword("Different1234");
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(badDto));

        // 2. Multi-User Strict Data Isolation
        User sahana = userService.getCurrentUser();

        JobApplication sahanaApp = new JobApplication("Bengaluru Tech Hub", "Lead Architect", "Whitefield", ApplicationStatus.APPLIED, LocalDate.now());
        JobApplication sahanaSaved = applicationService.saveApplication(sahanaApp, sahana);
        final Long sahanaAppId = sahanaSaved.getId();

        JobApplication priyaApp = new JobApplication("Priya Startup", "Junior Dev", "Koramangala", ApplicationStatus.APPLIED, LocalDate.now());
        JobApplication priyaSaved = applicationService.saveApplication(priyaApp, registeredUser);
        final Long priyaAppId = priyaSaved.getId();

        // Sahana can see her app, but NOT Priya's app
        var sahanaApps = applicationService.getAllApplications(sahana);
        assertTrue(sahanaApps.stream().anyMatch(a -> a.getId().equals(sahanaAppId)));
        assertFalse(sahanaApps.stream().anyMatch(a -> a.getId().equals(priyaAppId)));

        // Priya can see her app, but NOT Sahana's app
        var priyaApps = applicationService.getAllApplications(registeredUser);
        assertTrue(priyaApps.stream().anyMatch(a -> a.getId().equals(priyaAppId)));
        assertFalse(priyaApps.stream().anyMatch(a -> a.getId().equals(sahanaAppId)));

        // Priya cannot query Sahana's app by ID
        Optional<JobApplication> unauthorizedAccess = applicationRepository.findByIdAndUser(sahanaAppId, registeredUser);
        assertTrue(unauthorizedAccess.isEmpty(), "User B must not be able to retrieve User A's application");

        // 3. Password Reset Flow
        String resetToken = userService.createPasswordResetToken("priya.sharma@example.com");
        assertNotNull(resetToken);

        boolean resetSuccess = userService.resetPasswordWithToken(resetToken, "NewSecret@456");
        assertTrue(resetSuccess);

        // Token must be invalidated after single use
        boolean secondAttempt = userService.resetPasswordWithToken(resetToken, "AnotherSecret@789");
        assertFalse(secondAttempt, "Password reset token must be invalidated after one-time use");
    }
}
