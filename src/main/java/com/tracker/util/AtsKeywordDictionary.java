package com.tracker.util;

import java.util.*;

public class AtsKeywordDictionary {

    // Technical skills map: Display Name -> List of aliases/patterns to match
    public static final Map<String, List<String>> TECHNICAL_SKILLS = new LinkedHashMap<>();

    // Soft skills list
    public static final Map<String, List<String>> SOFT_SKILLS = new LinkedHashMap<>();

    // Common standard resume sections and their keywords
    public static final Map<String, List<String>> RESUME_SECTIONS = new LinkedHashMap<>();

    // Strong Action Verbs for ATS readability
    public static final Set<String> ACTION_VERBS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    // Recommended certifications mapped by domain
    public static final Map<String, List<String>> CERTIFICATIONS_BY_DOMAIN = new LinkedHashMap<>();

    static {
        // Core Languages
        TECHNICAL_SKILLS.put("Java", List.of("java", "core java", "java 8", "java 11", "java 17", "java 21", "j2se", "j2ee"));
        TECHNICAL_SKILLS.put("C", List.of("c", "c programming", "ansi c"));
        TECHNICAL_SKILLS.put("C++", List.of("c++", "cpp"));
        TECHNICAL_SKILLS.put("Python", List.of("python", "python3", "py"));
        TECHNICAL_SKILLS.put("JavaScript", List.of("javascript", "js", "ecmascript", "es6"));
        TECHNICAL_SKILLS.put("TypeScript", List.of("typescript", "ts"));
        TECHNICAL_SKILLS.put("C#", List.of("c#", "csharp", ".net"));
        TECHNICAL_SKILLS.put("SQL", List.of("sql", "t-sql", "pl/sql", "relational database"));
        TECHNICAL_SKILLS.put("HTML/CSS", List.of("html", "html5", "css", "css3"));
        TECHNICAL_SKILLS.put("Object-Oriented Programming", List.of("object-oriented", "oop", "object oriented programming"));
        TECHNICAL_SKILLS.put("Data Structures & Algorithms", List.of("data structures", "algorithms", "dsa"));

        // Frameworks & Libraries
        TECHNICAL_SKILLS.put("Spring Boot", List.of("spring boot", "springboot", "spring-boot"));
        TECHNICAL_SKILLS.put("Spring Framework", List.of("spring framework", "spring mvc", "spring data", "spring security"));
        TECHNICAL_SKILLS.put("Hibernate / JPA", List.of("hibernate", "jpa", "orm"));
        TECHNICAL_SKILLS.put("REST APIs", List.of("rest api", "restful", "rest apis", "rest web services", "api integration"));
        TECHNICAL_SKILLS.put("Microservices", List.of("microservices", "microservice architecture", "distributed systems"));
        TECHNICAL_SKILLS.put("React", List.of("react", "react.js", "reactjs"));
        TECHNICAL_SKILLS.put("Angular", List.of("angular", "angularjs"));
        TECHNICAL_SKILLS.put("Vue.js", List.of("vue", "vue.js", "vuejs"));
        TECHNICAL_SKILLS.put("Node.js", List.of("node.js", "nodejs", "express", "express.js"));
        TECHNICAL_SKILLS.put("Bootstrap", List.of("bootstrap", "bootstrap 5"));
        TECHNICAL_SKILLS.put("Tailwind CSS", List.of("tailwind", "tailwind css"));

        // Databases
        TECHNICAL_SKILLS.put("MySQL", List.of("mysql"));
        TECHNICAL_SKILLS.put("PostgreSQL", List.of("postgresql", "postgres"));
        TECHNICAL_SKILLS.put("MongoDB", List.of("mongodb", "mongo", "nosql"));
        TECHNICAL_SKILLS.put("Redis", List.of("redis", "caching"));
        TECHNICAL_SKILLS.put("Oracle DB", List.of("oracle db", "oracle database"));

        // DevOps, Cloud & Tools
        TECHNICAL_SKILLS.put("Git & GitHub", List.of("git", "github", "gitlab", "bitbucket", "version control"));
        TECHNICAL_SKILLS.put("Docker", List.of("docker", "containerization", "containers"));
        TECHNICAL_SKILLS.put("Kubernetes", List.of("kubernetes", "k8s"));
        TECHNICAL_SKILLS.put("AWS", List.of("aws", "amazon web services", "ec2", "s3", "lambda"));
        TECHNICAL_SKILLS.put("Azure", List.of("azure", "microsoft azure"));
        TECHNICAL_SKILLS.put("CI/CD", List.of("ci/cd", "continuous integration", "jenkins", "github actions"));
        TECHNICAL_SKILLS.put("Maven / Gradle", List.of("maven", "gradle", "build tools"));
        TECHNICAL_SKILLS.put("JUnit / Mockito", List.of("junit", "mockito", "unit testing", "test driven development", "tdd"));
        TECHNICAL_SKILLS.put("Kafka / RabbitMQ", List.of("kafka", "rabbitmq", "message queue", "event streaming"));
        TECHNICAL_SKILLS.put("Linux / Bash", List.of("linux", "unix", "bash", "shell scripting"));

        // Soft Skills
        SOFT_SKILLS.put("Communication", List.of("communication", "verbal communication", "written communication", "presentation"));
        SOFT_SKILLS.put("Problem Solving", List.of("problem solving", "problem-solving", "troubleshooting", "debugging", "analytical"));
        SOFT_SKILLS.put("Team Collaboration", List.of("teamwork", "collaboration", "cross-functional", "team player"));
        SOFT_SKILLS.put("Agile & Scrum", List.of("agile", "scrum", "sprints", "jira", "kanban"));
        SOFT_SKILLS.put("Time Management", List.of("time management", "prioritization", "multitasking", "deadlines"));
        SOFT_SKILLS.put("Leadership", List.of("leadership", "mentoring", "initiative", "team lead", "guidance"));
        SOFT_SKILLS.put("Critical Thinking", List.of("critical thinking", "decision making", "strategic thinking"));
        SOFT_SKILLS.put("Adaptability", List.of("adaptability", "quick learner", "fast learner", "flexibility"));

        // Resume Standard Sections
        RESUME_SECTIONS.put("Contact Information", List.of("email", "phone", "linkedin", "github", "address", "contact"));
        RESUME_SECTIONS.put("Professional Summary", List.of("summary", "professional summary", "career objective", "about me", "profile"));
        RESUME_SECTIONS.put("Work Experience", List.of("experience", "work experience", "employment", "professional experience", "internship"));
        RESUME_SECTIONS.put("Education", List.of("education", "academic", "university", "college", "degree", "bachelor", "master"));
        RESUME_SECTIONS.put("Technical Skills", List.of("skills", "technical skills", "technologies", "proficiencies", "core competencies"));
        RESUME_SECTIONS.put("Projects", List.of("projects", "personal projects", "academic projects", "key projects", "work samples"));
        RESUME_SECTIONS.put("Certifications", List.of("certifications", "certificates", "licenses", "courses", "accreditations"));

        // Action Verbs
        ACTION_VERBS.addAll(Arrays.asList(
            "built", "developed", "designed", "implemented", "created", "architected", "engineered",
            "optimized", "improved", "enhanced", "reduced", "increased", "deployed", "automated",
            "integrated", "refactored", "maintained", "orchestrated", "spearheaded", "collaborated",
            "managed", "led", "solved", "analyzed", "delivered", "configured", "accelerated"
        ));

        // Domain Certifications
        CERTIFICATIONS_BY_DOMAIN.put("Java & Backend", List.of(
            "Oracle Certified Professional: Java SE 17 / 21 Developer",
            "Spring Certified Professional (Broadcom / VMware)",
            "AWS Certified Developer - Associate",
            "Docker Certified Associate (DCA)"
        ));
        CERTIFICATIONS_BY_DOMAIN.put("Cloud & DevOps", List.of(
            "AWS Certified Solutions Architect - Associate",
            "Microsoft Certified: Azure Fundamentals (AZ-900) or Developer (AZ-204)",
            "Certified Kubernetes Administrator (CKA)",
            "HashiCorp Certified: Terraform Associate"
        ));
        CERTIFICATIONS_BY_DOMAIN.put("Frontend & Full Stack", List.of(
            "Meta Front-End Developer Professional Certificate (Coursera)",
            "Full Stack Web Development with React Specialization",
            "FreeCodeCamp Responsive Web Design & JavaScript Algorithms"
        ));
        CERTIFICATIONS_BY_DOMAIN.put("General & Data", List.of(
            "CompTIA Security+ or Linux+",
            "Google Cloud Associate Cloud Engineer",
            "PostgreSQL Certified Associate"
        ));
    }
}
