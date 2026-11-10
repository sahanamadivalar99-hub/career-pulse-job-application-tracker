package com.tracker.config;

import com.tracker.dto.ResumeAnalysisResult;
import com.tracker.model.*;
import com.tracker.repository.*;
import com.tracker.service.ResumeAnalyzerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Data Loader initializing realistic demonstration data for a job seeker
 * based in Bengaluru, Karnataka, India.
 * 
 * Features idempotent seeding: runs safely on startup without duplicating records
 * or overwriting existing user modifications, and automatically attaches orphaned records
 * to the default demo user.
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private ResumeAnalyzerService analyzerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private JobMatchRepository jobMatchRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        User demoUser = seedUserProfile();
        migrateOrphanedRecords(demoUser);
        seedBengaluruCompanies();
        seedBengaluruApplications(demoUser);
        seedBengaluruReminders(demoUser);
        seedFresherSampleResume(demoUser);
        seedInitialChatAssistant(demoUser);
    }

    private User seedUserProfile() {
        User user = userRepository.findByEmailIgnoreCase("sahana.bengaluru@example.com")
                .or(() -> userRepository.findByUsername("developer"))
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername("developer");
                    return u;
                });

        user.setFullName("Sahana");
        user.setEmail("sahana.bengaluru@example.com");
        user.setPassword(passwordEncoder.encode("Sahana@123"));
        user.setPhone("+91 98765 43210");
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        user.setTargetRole("Java Full Stack Developer / AI & Data Engineer");
        user.setLocation("Bengaluru, Karnataka, India");
        user.setEducation("B.E. Engineering Student");
        user.setExperience("Fresher");
        user.setPreferredLocation("Bengaluru");
        user.setPreferredEmployment("Internship and Full-Time");
        return userRepository.save(user);
    }

    private void migrateOrphanedRecords(User user) {
        // Associate pre-existing orphaned applications to demo user
        List<JobApplication> orphanedApps = applicationRepository.findByUserIsNull();
        for (JobApplication app : orphanedApps) {
            app.setUser(user);
            applicationRepository.save(app);
        }

        // Associate pre-existing orphaned resumes
        List<Resume> orphanedResumes = resumeRepository.findByUserIsNull();
        for (Resume r : orphanedResumes) {
            r.setUser(user);
            resumeRepository.save(r);
        }

        // Associate pre-existing orphaned reminders
        List<Reminder> orphanedReminders = reminderRepository.findByUserIsNull();
        for (Reminder rem : orphanedReminders) {
            rem.setUser(user);
            reminderRepository.save(rem);
        }

        // Associate pre-existing orphaned chat messages
        List<ChatMessage> orphanedMsgs = chatMessageRepository.findByUserIsNull();
        for (ChatMessage msg : orphanedMsgs) {
            msg.setUser(user);
            chatMessageRepository.save(msg);
        }

        // Associate pre-existing orphaned job matches
        List<JobMatch> orphanedMatches = jobMatchRepository.findByUserIsNull();
        for (JobMatch match : orphanedMatches) {
            match.setUser(user);
            jobMatchRepository.save(match);
        }
    }

    private void seedBengaluruCompanies() {
        List<Company> companies = List.of(
            new Company("Infosys", "IT Services & Enterprise Consulting", "Electronic City, Bengaluru", "https://www.infosys.com/careers"),
            new Company("Wipro", "Cloud Solutions & Cognitive IT", "Sarjapur Road, Bengaluru", "https://careers.wipro.com"),
            new Company("TCS", "Global Enterprise Solutions", "Whitefield, Bengaluru", "https://www.tcs.com/careers"),
            new Company("Accenture", "Management & Technology Consulting", "Outer Ring Road, Bengaluru", "https://www.accenture.com/in-en/careers"),
            new Company("IBM", "Hybrid Cloud & Enterprise Systems", "Manyata Tech Park, Bengaluru", "https://www.ibm.com/in-en/employment"),
            new Company("Google", "Cloud, AI & Search Platforms", "Old Airport Road / ORR, Bengaluru", "https://careers.google.com"),
            new Company("Microsoft", "Enterprise Cloud & Azure Platforms", "Lavelle Road / Bellandur, Bengaluru", "https://careers.microsoft.com"),
            new Company("Amazon", "E-Commerce & AWS Cloud Infrastructure", "World Trade Center, Rajajinagar, Bengaluru", "https://amazon.jobs"),
            new Company("Flipkart", "E-Commerce & Retail Technology", "Devarabeesanahalli, Outer Ring Road, Bengaluru", "https://www.flipkartcareers.com"),
            new Company("PhonePe", "Fintech & Digital Payments (UPI)", "Bellandur, Bengaluru", "https://www.phonepe.com/careers"),
            new Company("Razorpay", "Payments Infrastructure & Neo-Banking", "Koramangala, Bengaluru", "https://razorpay.com/jobs"),
            new Company("Bosch", "Mobility Solutions & IoT Engineering", "Adugodi / Electronic City, Bengaluru", "https://www.bosch.in/careers")
        );

        for (Company c : companies) {
            if (companyRepository.findByNameIgnoreCase(c.getName()).isEmpty()) {
                c.setRating(4.5);
                c.setNotes("[Demo Record] Prominent technology employer in Bengaluru hiring freshers and interns.");
                companyRepository.save(c);
            }
        }
    }

    private void seedBengaluruApplications(User user) {
        // 1. Google - Software Engineer Intern (INTERVIEW)
        createApplicationIfNotExists(user,
            "Google", "Software Engineer Intern", "Bengaluru, Karnataka (Hybrid)",
            ApplicationStatus.INTERVIEW, LocalDate.of(2026, 10, 18),
            "₹80,000 - ₹1,00,000 / month",
            "Summer Engineering Internship: Algorithms, Data Structures, core Java, and scalable cloud microservices.",
            "[Demo Record] Technical Round 1 cleared successfully. System design & coding interview on Nov 14th.",
            LocalDateTime.of(2026, 11, 14, 14, 0), LocalDate.of(2026, 11, 15), "https://careers.google.com"
        );

        // 2. Infosys - Java Developer Intern (OFFER)
        createApplicationIfNotExists(user,
            "Infosys", "Java Developer Intern", "Electronic City, Bengaluru",
            ApplicationStatus.OFFER, LocalDate.of(2026, 9, 12),
            "₹25,000 - ₹35,000 / month",
            "Developing enterprise backend services using Java, Spring Boot, MySQL, and REST APIs for banking and financial clients.",
            "[Demo Record] Received 6-month internship offer letter with PPO conversion opportunity. Formal acceptance pending.",
            null, LocalDate.of(2026, 11, 20), "https://www.infosys.com/careers"
        );

        // 3. Accenture - Graduate Engineer Trainee (INTERVIEW)
        createApplicationIfNotExists(user,
            "Accenture", "Graduate Engineer Trainee", "Outer Ring Road, Bengaluru",
            ApplicationStatus.INTERVIEW, LocalDate.of(2026, 10, 25),
            "₹4,50,000 - ₹6,50,000 PA",
            "Graduate Engineer Trainee program: Java full-stack application development, agile delivery, cloud foundations, and automated testing.",
            "[Demo Record] Technical screen cleared. Final round with Delivery Lead scheduled for Nov 16th.",
            LocalDateTime.of(2026, 11, 16, 11, 30), LocalDate.of(2026, 11, 17), "https://www.accenture.com/in-en/careers"
        );

        // 4. PhonePe - Junior Java Developer (IN_REVIEW)
        createApplicationIfNotExists(user,
            "PhonePe", "Junior Java Developer", "Bellandur, Bengaluru",
            ApplicationStatus.IN_REVIEW, LocalDate.of(2026, 11, 2),
            "₹8,00,000 - ₹12,00,000 PA",
            "Junior backend engineer to work on high-throughput UPI transaction processing microservices with Java, Spring Boot, Kafka, and Redis.",
            "[Demo Record] Applied via employee referral. Coding test completed on HackerRank with 100% score.",
            null, LocalDate.of(2026, 11, 12), "https://www.phonepe.com/careers"
        );

        // 5. Razorpay - Full Stack Developer Intern (APPLIED)
        createApplicationIfNotExists(user,
            "Razorpay", "Full Stack Developer Intern", "Koramangala, Bengaluru",
            ApplicationStatus.APPLIED, LocalDate.of(2026, 11, 6),
            "₹40,000 - ₹50,000 / month",
            "Full stack engineering internship building merchant checkout dashboards with React, Java, Spring Boot, and PostgreSQL.",
            "[Demo Record] Application submitted via Razorpay careers portal with portfolio project links.",
            null, LocalDate.of(2026, 11, 18), "https://razorpay.com/jobs"
        );

        // 6. Flipkart - Software Engineer Intern (IN_REVIEW)
        createApplicationIfNotExists(user,
            "Flipkart", "Software Engineer Intern", "Outer Ring Road, Bengaluru",
            ApplicationStatus.IN_REVIEW, LocalDate.of(2026, 10, 28),
            "₹50,000 - ₹75,000 / month",
            "Building scalable catalog indexing and order management subsystems using Java, Spring Cloud, Elasticsearch, and MySQL.",
            "[Demo Record] Profile shortlisted by campus talent acquisition team; technical interview to be scheduled.",
            null, LocalDate.of(2026, 11, 13), "https://www.flipkartcareers.com"
        );

        // 7. Amazon - AI/ML Intern (APPLIED)
        createApplicationIfNotExists(user,
            "Amazon", "AI/ML Intern", "World Trade Center, Bengaluru",
            ApplicationStatus.APPLIED, LocalDate.of(2026, 11, 4),
            "₹60,000 - ₹80,000 / month",
            "Internship exploring applied machine learning, LLM fine-tuning, Python, data pipelines, and AWS SageMaker.",
            "[Demo Record] Tailored resume submitted for AWS Generative AI team.",
            null, LocalDate.of(2026, 11, 19), "https://amazon.jobs"
        );

        // 8. TCS - Graduate Engineer Trainee (APPLIED)
        createApplicationIfNotExists(user,
            "TCS", "Graduate Engineer Trainee", "Whitefield, Bengaluru",
            ApplicationStatus.APPLIED, LocalDate.of(2026, 10, 10),
            "₹3,60,000 - ₹4,20,000 PA",
            "TCS Digital / Ninja recruitment track for B.E. freshers in enterprise software engineering, Java, and cloud platforms.",
            "[Demo Record] TCS NQT score submitted; awaiting interview call.",
            null, LocalDate.of(2026, 11, 22), "https://www.tcs.com/careers"
        );

        // 9. Wipro - Data Analyst Intern (APPLIED)
        createApplicationIfNotExists(user,
            "Wipro", "Data Analyst Intern", "Sarjapur Road, Bengaluru",
            ApplicationStatus.APPLIED, LocalDate.of(2026, 11, 1),
            "₹20,000 - ₹30,000 / month",
            "Data analytics internship: SQL querying, business intelligence dashboards, Python data transformation, and reporting.",
            "[Demo Record] Applied through LinkedIn internship listing with sample SQL project portfolio.",
            null, LocalDate.of(2026, 11, 15), "https://careers.wipro.com"
        );

        // 10. IBM - Python Developer Intern (IN_REVIEW)
        createApplicationIfNotExists(user,
            "IBM", "Python Developer Intern", "Manyata Tech Park, Bengaluru",
            ApplicationStatus.IN_REVIEW, LocalDate.of(2026, 10, 15),
            "₹30,000 - ₹40,000 / month",
            "Developing automation scripts, REST services, and integration testing using Python, FastAPI, Docker, and Linux.",
            "[Demo Record] Completed technical assessment with top percentile.",
            null, LocalDate.of(2026, 11, 14), "https://www.ibm.com/in-en/employment"
        );

        // 11. Microsoft - Software Engineer Intern (REJECTED)
        createApplicationIfNotExists(user,
            "Microsoft", "Software Engineer Intern", "Lavelle Road, Bengaluru",
            ApplicationStatus.REJECTED, LocalDate.of(2026, 8, 20),
            "₹80,000 - ₹1,00,000 / month",
            "Summer Internship in Azure Developer Division. Position required extensive C# / .NET experience.",
            "[Demo Record] Received polite automated rejection. Redirected focus toward Java Full-Stack ecosystem.",
            null, null, "https://careers.microsoft.com"
        );

        // 12. Bosch - Graduate Engineer Trainee (REJECTED)
        createApplicationIfNotExists(user,
            "Bosch", "Graduate Engineer Trainee", "Electronic City, Bengaluru",
            ApplicationStatus.REJECTED, LocalDate.of(2026, 9, 5),
            "₹5,00,000 - ₹6,50,000 PA",
            "Automotive embedded systems and diagnostics role requiring CAN protocol and C/Assembly expertise.",
            "[Demo Record] Position prioritized electronics/hardware background. Gained valuable interview practice.",
            null, null, "https://www.bosch.in/careers"
        );
    }

    private void createApplicationIfNotExists(User user, String companyName, String jobRole, String location,
                                              ApplicationStatus status, LocalDate applicationDate,
                                              String salaryRange, String description, String notes,
                                              LocalDateTime interviewDate, LocalDate nextFollowUpDate, String link) {
        if (!applicationRepository.existsByCompanyNameAndJobRoleAndUser(companyName, jobRole, user)) {
            JobApplication app = new JobApplication(companyName, jobRole, location, status, applicationDate);
            app.setUser(user);
            app.setSalaryRange(salaryRange);
            app.setJobDescription(description);
            app.setNotes(notes);
            app.setInterviewDate(interviewDate);
            app.setNextFollowUpDate(nextFollowUpDate);
            app.setJobLink(link);
            applicationRepository.save(app);
        }
    }

    private void seedBengaluruReminders(User user) {
        createReminderIfNotExists(user,
            "Java Developer Interview – Bengaluru",
            ReminderType.INTERVIEW,
            LocalDate.of(2026, 11, 14),
            LocalTime.of(14, 0),
            "Google Software Engineer Intern Round 2: System design, concurrency, Java 21, and problem-solving."
        );

        createReminderIfNotExists(user,
            "Follow-up with HR – Infosys",
            ReminderType.FOLLOW_UP,
            LocalDate.of(2026, 11, 15),
            LocalTime.of(11, 0),
            "Send confirmation email to HR regarding Infosys Java Developer Internship PPO offer letter."
        );

        createReminderIfNotExists(user,
            "Technical Interview – Accenture",
            ReminderType.INTERVIEW,
            LocalDate.of(2026, 11, 16),
            LocalTime.of(11, 30),
            "Accenture Graduate Engineer Trainee final round: Java full-stack foundations and cloud readiness."
        );

        createReminderIfNotExists(user,
            "Resume Improvement Deadline",
            ReminderType.DEADLINE,
            LocalDate.of(2026, 11, 18),
            LocalTime.of(18, 0),
            "Complete Spring Boot REST API project and add GitHub live demo link to resume before next drive."
        );
    }

    private void createReminderIfNotExists(User user, String title, ReminderType type, LocalDate date, LocalTime time, String notes) {
        if (!reminderRepository.existsByTitleAndUser(title, user)) {
            Reminder r = new Reminder(title, type, date, time, notes);
            r.setUser(user);
            reminderRepository.save(r);
        }
    }

    private void seedFresherSampleResume(User user) {
        String fileName = "Sahana_Fresher_Java_Bengaluru_Resume.pdf";
        if (!resumeRepository.existsByOriginalFileNameAndUser(fileName, user)) {
            String sampleResumeText =
                "SAHANA | B.E. COMPUTER SCIENCE & ENGINEERING (FRESHER)\n" +
                "Location: Bengaluru, Karnataka, India | Email: sahana.bengaluru@example.com | Phone: +91 98765 43210\n" +
                "GitHub: github.com/sahana-dev | LinkedIn: linkedin.com/in/sahana-bengaluru-dev\n\n" +
                "CAREER OBJECTIVE\n" +
                "Enthusiastic and motivated B.E. Computer Science graduate seeking an entry-level position as a " +
                "Java Full Stack Developer / Software Engineer Intern in Bengaluru. Eager to apply strong problem-solving skills, " +
                "object-oriented design principles, and modern web technologies to build resilient enterprise software.\n\n" +
                "EDUCATION\n" +
                "Bachelor of Engineering (B.E.) in Computer Science & Engineering | 2022 – 2026\n" +
                "Visvesvaraya Technological University (VTU), Bengaluru, Karnataka, India | CGPA: 8.7 / 10\n\n" +
                "TECHNICAL SKILLS\n" +
                "- Programming Languages: Java, C, Python, JavaScript\n" +
                "- Web Technologies: HTML5, CSS3, JavaScript\n" +
                "- Database & Querying: SQL, Relational Database Management Systems\n" +
                "- Core Concepts: Object-Oriented Programming (OOP), Data Structures, Algorithms, Software Engineering Principles\n\n" +
                "ACADEMIC & DEMONSTRATION PROJECTS\n" +
                "1. Smart Campus Job Application Portal\n" +
                "- Developed an interactive job tracking web application using Java, SQL, HTML, and CSS.\n" +
                "- Implemented responsive user dashboards, CRUD operations, and relational database schema for student placement drives.\n" +
                "- Optimized SQL queries for student profile indexing.\n\n" +
                "2. Student Performance Analytics Tool\n" +
                "- Built a desktop utility in Java and Python to analyze semester examination results.\n" +
                "- Processed CSV academic datasets and generated graphical distribution reports.\n\n" +
                "ACHIEVEMENTS & CERTIFICATIONS\n" +
                "- Winner, Inter-Collegiate Java Hackathon, Bengaluru (2025).\n" +
                "- Active participant in Coding Club and technical seminars on cloud computing.\n";

            Resume sampleResume = new Resume(
                "sample_sahana_bengaluru.pdf",
                fileName,
                "application/pdf",
                128400L,
                "uploads/resumes/sample_sahana_bengaluru.pdf",
                sampleResumeText
            );
            sampleResume.setUser(user);
            sampleResume = resumeRepository.save(sampleResume);

            // Generate deterministic transparent analysis
            ResumeAnalysisResult analysisResult = analyzerService.analyzeResumeText(sampleResumeText);
            analyzerService.saveAnalysis(sampleResume, analysisResult);
        }
    }

    private void seedInitialChatAssistant(User user) {
        if (chatMessageRepository.findAllByUserOrderByCreatedAtAsc(user).isEmpty()) {
            ChatMessage msg = new ChatMessage(
                "assistant",
                "Namaskara Sahana! I am your AI Career Assistant. I can help you prepare for technical interviews at top Bengaluru tech companies (Google, Infosys, Accenture, PhonePe, Flipkart), identify high-demand skills for Java Developer internships, recommend portfolio projects, and critique your resume. How can I assist your career journey today?",
                "RULE_BASED"
            );
            msg.setUser(user);
            chatMessageRepository.save(msg);
        }
    }
}
