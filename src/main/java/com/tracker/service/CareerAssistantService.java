package com.tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.dto.ChatMessageDto;
import com.tracker.model.ChatMessage;
import com.tracker.model.User;
import com.tracker.repository.ChatMessageRepository;
import com.tracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class CareerAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(CareerAssistantService.class);

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Value("${gemini.api.key:}")
    private String configuredApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public ChatMessageDto processUserMessage(String userPrompt) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            return new ChatMessageDto("assistant", "Please ask a question regarding your job search, skills, or resume.", "RULE_BASED");
        }

        User currentUser = userService.getCurrentUser();

        // Save user message
        ChatMessage userMsg = new ChatMessage("user", userPrompt.trim(), "USER");
        userMsg.setUser(currentUser);
        chatMessageRepository.save(userMsg);

        // Determine if an API key is available (from User entity or application.properties)
        String activeApiKey = getActiveApiKey();

        ChatMessageDto responseDto;
        if (activeApiKey != null && !activeApiKey.isBlank()) {
            try {
                String aiResponse = callGeminiLlmApi(userPrompt.trim(), activeApiKey);
                if (aiResponse != null && !aiResponse.isBlank()) {
                    responseDto = new ChatMessageDto("assistant", aiResponse, "LLM");
                    saveAssistantMessage(aiResponse, "LLM", currentUser);
                    return responseDto;
                }
            } catch (Exception e) {
                logger.warn("LLM API call failed: {}. Falling back to Java rule-based career engine.", e.getMessage());
            }
        }

        // Fallback to Rule-Based Engine
        String ruleResponse = generateRuleBasedResponse(userPrompt.trim());
        responseDto = new ChatMessageDto("assistant", ruleResponse, "RULE_BASED");
        saveAssistantMessage(ruleResponse, "RULE_BASED", currentUser);
        return responseDto;
    }

    private void saveAssistantMessage(String content, String provider, User user) {
        ChatMessage assistantMsg = new ChatMessage("assistant", content, provider);
        assistantMsg.setUser(user);
        chatMessageRepository.save(assistantMsg);
    }

    public List<ChatMessage> getChatHistory() {
        return chatMessageRepository.findAllByUserOrderByCreatedAtAsc(userService.getCurrentUser());
    }

    @Transactional
    public void clearHistory() {
        chatMessageRepository.deleteAllByUser(userService.getCurrentUser());
    }

    private String getActiveApiKey() {
        User user = userService.getCurrentUser();
        if (user != null && user.getGeminiApiKey() != null && !user.getGeminiApiKey().isBlank()) {
            return user.getGeminiApiKey();
        }
        if (configuredApiKey != null && !configuredApiKey.isBlank() && !configuredApiKey.equals("YOUR_API_KEY")) {
            return configuredApiKey;
        }
        return null;
    }

    /**
     * Calls Google Gemini Generative Language API via standard Java HttpURLConnection
     */
    private String callGeminiLlmApi(String prompt, String apiKey) throws Exception {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        String systemInstruction = "You are a senior tech career coach and Java architect. Provide clear, concise, actionable advice for software engineering candidates, covering resumes, interviews, tech stacks, and career growth. Format your response cleanly using Markdown bullet points.";

        Map<String, Object> contentsPart = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", systemInstruction + "\n\nUser Question: " + prompt)
                ))
            )
        );

        String jsonBody = objectMapper.writeValueAsString(contentsPart);

        java.net.URL url = java.net.URI.create(endpoint).toURL();
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);

        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        int statusCode = conn.getResponseCode();
        if (statusCode == 200) {
            try (java.io.InputStream is = conn.getInputStream()) {
                JsonNode root = objectMapper.readTree(is);
                JsonNode textNode = root.at("/candidates/0/content/parts/0/text");
                if (!textNode.isMissingNode()) {
                    return textNode.asText();
                }
            }
        } else {
            logger.warn("Gemini API returned status {}", statusCode);
        }
        return null;
    }

    /**
     * Comprehensive Rule-Based Expert System for Java Developer Careers
     */
    public String generateRuleBasedResponse(String prompt) {
        String p = prompt.toLowerCase(Locale.ROOT);

        if (p.contains("skill") && (p.contains("java") || p.contains("backend") || p.contains("learn"))) {
            return "### Essential Skills for Java Developers in 2026\n\n" +
                   "To stand out for modern Java Backend and Full-Stack roles, focus on these five core pillars:\n\n" +
                   "1. **Core Java Deep Dive:** Master Java 17 / 21 features — Records, Pattern Matching, Sealed Classes, Virtual Threads (Project Loom), Stream API, and Generics.\n" +
                   "2. **Spring Ecosystem:** Spring Boot 3.x, Spring Data JPA, Spring Security (JWT / OAuth2), and Spring Cloud for Microservices.\n" +
                   "3. **Database & Persistence:** Relational database design (MySQL / PostgreSQL), SQL optimization, Hibernate indexing, and caching with Redis.\n" +
                   "4. **API Design & Architecture:** Clean RESTful APIs, OpenAPI/Swagger documentation, gRPC/Kafka event streaming, and architectural patterns (Layered, CQRS).\n" +
                   "5. **DevOps & Testing:** Unit testing with JUnit 5 & Mockito, Docker containerization, and basic CI/CD with GitHub Actions.\n\n" +
                   "> *Note: This response was generated by the built-in Java Career Knowledge Engine. To enable real-time Gemini LLM answers, add your API key in Settings.*";
        }

        if (p.contains("resume") || p.contains("cv") || p.contains("ats")) {
            return "### How to Optimize Your Resume for ATS & Hiring Managers\n\n" +
                   "1. **Clean Single-Column Layout:** Use simple headings (Summary, Skills, Experience, Projects, Education). Avoid tables, text boxes, or graphics that confuse ATS parsers.\n" +
                   "2. **The X-Y-Z Accomplishment Formula:** Write bullet points as: *Accomplished [X], as measured by [Y], by doing [Z]*.\n" +
                   "   - *Weak:* 'Created REST endpoints for products.'\n" +
                   "   - *Strong:* 'Engineered 12 RESTful microservice endpoints with Spring Boot and Redis caching, reducing API response latency by 42% for 5,000+ daily users.'\n" +
                   "3. **Targeted Tech Keywords:** Categorize skills clearly (e.g. Languages, Frameworks, Cloud/DevOps, Databases).\n" +
                   "4. **Active GitHub & Demo Links:** Recruiters click GitHub links to inspect repository cleanliness, commit history, and README documentation.\n" +
                   "5. **Use our Resume Analyzer:** Upload your PDF in the **Resume Analyzer** tab to view your ATS score and personalized breakdown instantly!\n\n" +
                   "> *Note: Generated by the built-in Java Career Knowledge Engine.*";
        }

        if (p.contains("project") || p.contains("portfolio") || p.contains("build")) {
            return "### Top 3 Industry-Grade Projects for Java Developers\n\n" +
                   "Avoid generic to-do apps and build projects that reflect real-world engineering challenges:\n\n" +
                   "1. **Job Application Tracker & Resume ATS Analyzer (Like this app!):**\n" +
                   "   - *Tech:* Spring Boot 3, Spring Data JPA, MySQL, Apache PDFBox, Thymeleaf / Bootstrap 5.\n" +
                   "   - *Highlight:* PDF parsing, deterministic text analysis algorithms, Kanban workflow, and responsive dashboards.\n\n" +
                   "2. **Distributed E-Commerce Microservices:**\n" +
                   "   - *Tech:* Spring Cloud, API Gateway, Eureka/Consul, Apache Kafka, Docker Compose, PostgreSQL.\n" +
                   "   - *Highlight:* Event-driven order processing, saga pattern for transactions, and payment gateway integration.\n\n" +
                   "3. **Real-Time Financial / Stock Portfolio Tracker:**\n" +
                   "   - *Tech:* Java 21 Virtual Threads, WebSockets, Redis cache, JWT Authentication.\n" +
                   "   - *Highlight:* Handling high-throughput concurrent WebSocket feeds and sub-millisecond in-memory caching.\n\n" +
                   "> *Note: Generated by the built-in Java Career Knowledge Engine.*";
        }

        if (p.contains("interview") || p.contains("prepare") || p.contains("dsa") || p.contains("round")) {
            return "### Complete Technical Interview Preparation Strategy\n\n" +
                   "**Phase 1: Core Java & Spring Concepts (Days 1–10)**\n" +
                   "- JVM internals: ClassLoader, Garbage Collection (G1/ZGC), Memory areas (Heap, Stack, Metaspace).\n" +
                   "- Concurrency: Thread lifecycle, `synchronized`, `ReentrantLock`, Virtual Threads, `CompletableFuture`.\n" +
                   "- Spring: Dependency Injection, Bean Scopes, Bean lifecycle, Transaction management (`@Transactional` propagation).\n\n" +
                   "**Phase 2: Data Structures & Algorithms (Days 11–25)**\n" +
                   "- Master HashMaps, LinkedLists, Two-Pointers, Sliding Window, Trees, and BFS/DFS graphs.\n" +
                   "- Solve 50 curated LeetCode Medium problems in Java.\n\n" +
                   "**Phase 3: System Design & Databases (Days 26–35)**\n" +
                   "- Database Normalization, Indexing (B-Tree), ACID properties, Connection Pooling (HikariCP).\n" +
                   "- High-level design: URL Shortener, Rate Limiter, Notification Service.\n\n" +
                   "**Phase 4: Behavioral & Storytelling (Days 36–40)**\n" +
                   "- Prepare 4 stories using the STAR method (Situation, Task, Action, Result) showcasing leadership, problem-solving, and overcoming obstacles.\n\n" +
                   "> *Note: Generated by the built-in Java Career Knowledge Engine.*";
        }

        if (p.contains("certif") || p.contains("credential") || p.contains("exam")) {
            return "### Most Valuable Certifications for Java & Cloud Careers\n\n" +
                   "1. **Oracle Certified Professional: Java SE 17/21 Developer (1Z0-829 / 1Z0-830):**\n" +
                   "   - Validates deep mastery of modern Java syntax, streams, modularity, and concurrency.\n" +
                   "2. **Spring Certified Professional:**\n" +
                   "   - Highly regarded for enterprise backend developers demonstrating proficiency in Spring Core, Data, and MVC.\n" +
                   "3. **AWS Certified Developer – Associate (DVA-C02) or Solutions Architect (SAA-C03):**\n" +
                   "   - Proves you understand how to deploy, manage, and scale Java applications in the cloud.\n" +
                   "4. **Docker / Kubernetes (CKAD - Certified Kubernetes Application Developer):**\n" +
                   "   - Demonstrates strong containerization and modern cloud-native deployment capabilities.\n\n" +
                   "> *Note: Generated by the built-in Java Career Knowledge Engine.*";
        }

        // Generic helpful career guidance
        return "### Career Assistant Insights\n\n" +
               "Here are quick tips tailored to your job search:\n\n" +
               "- **Track Every Application:** Use our **Applications** Kanban board to monitor progress and never miss an interview follow-up.\n" +
               "- **Target Your Resumes:** Use our **Job Matcher** tool to compare your resume against the target job description and ensure you reach 70%+ match.\n" +
               "- **Set Reminders:** Navigate to **Reminders** to schedule interview preparation sessions, online assessments, and recruiter check-ins.\n\n" +
               "Feel free to ask me:\n" +
               "- *'What skills should I learn for Java Developer jobs?'*\n" +
               "- *'How can I improve my resume for ATS?'*\n" +
               "- *'What projects should I build to impress recruiters?'*\n" +
               "- *'How should I prepare for technical interviews?'*\n" +
               "- *'Which certifications are useful?'*\n\n" +
               "> *Note: This response was generated by the built-in Java Career Knowledge Engine. To enable real-time Gemini LLM answers, add your API key in Settings.*";
    }
}
