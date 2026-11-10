# CareerPulse – Smart Job Application Tracker with AI Resume Analyzer

A full-stack, enterprise-grade web application engineered in **Java 21** and **Spring Boot 3** with **Spring Security 6**. Designed specifically for students, software engineers, and job seekers to track job applications, manage interview schedules, visualize pipeline stages with a modern Kanban board, extract and analyze PDF resumes via **Apache PDFBox**, identify skill gaps against real job descriptions, and receive career coaching through an **AI Career Assistant**.

---

## 🌟 Quick Demonstration Access

| Role | Email / Username | Password | Notes |
|---|---|---|---|
| **Primary Demo Account** | `sahana.bengaluru@example.com` *(or `developer`)* | `Sahana@123` | Pre-loaded with 12 Bengaluru tech company applications, sample PDF resume, and interview schedules |
| **New User Registration** | *Any valid email* | *User choice (min 6 chars)* | Isolated workspace; new users receive independent dashboards and pipelines |

- **Local Access URL:** [http://localhost:8080](http://localhost:8080)
- **H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console) *(JDBC URL: `jdbc:h2:file:./data/jobtrackerdb`)*

---

## 🚀 Key Features & Modules

### 🔒 Module 1: Professional Authentication & Multi-User Security
- **Spring Security 6 Integration:** Role-based access control, session fixation protection, and CSRF token verification on all mutating requests.
- **Modern SaaS Login Page (`/login`):** Purple & light lavender aesthetic, email/username login, password show/hide eye toggle, remember-me (14 days), and one-click demo credentials auto-fill.
- **User Registration (`/register`):** Dynamic password strength indicator, confirmation matching, duplicate email rejection, and BCrypt password hashing.
- **Forgot & Reset Password Flow (`/forgot-password`, `/reset-password`):** Secure UUID-based one-time reset tokens valid for 1 hour.
- **Strict Multi-User Data Isolation:** Every application, resume, reminder, chat message, and job match is strictly linked to the authenticated user. User A cannot access or view User B's private data.
- **Personalized Header & Profile (`/profile`):** User avatar initial, location, target role, and account summary.

### 📊 Module 2: Executive Dashboard (`/dashboard`)
- **Key Metrics:** Total Applications, In Progress, Interviews Scheduled, Offers Received, and Rejections.
- **Interactive Visualizations:**
  - *Application Status Breakdown:* Chart.js doughnut chart showing real-time application distribution.
  - *Monthly Activity Trend:* 6-month historical activity bar chart dynamically calculated from database records.
- **Upcoming Interviews:** Chronological alerts for upcoming technical screens (Google, Accenture, etc.).
- **Bengaluru Candidate Context:** Pre-configured with realistic opportunities across Bengaluru tech corridors (Whitefield, Electronic City, Koramangala, Outer Ring Road).

### 📋 Module 3: Job Application Management & Kanban Board (`/applications`)
- **Full CRUD Operations:** Add, view detail, edit, and delete job applications.
- **Rich Data Fields:** Company Name, Job Role, Location, Salary Range, Application Date, Interview Schedule, Follow-Up Date, Portal URL, and Notes.
- **HTML5 Drag & Drop Kanban Board (`/applications/kanban`):** Real-time pipeline with 5 stages (`APPLIED`, `IN_REVIEW`, `INTERVIEW`, `OFFER`, `REJECTED`) backed by asynchronous AJAX state updates.
- **Multi-Criteria Search & Filtering:** Filter by keyword (company, role, location) and stage.

### 📄 Module 4: AI Resume Analyzer (Apache PDFBox Engine) (`/resume-analyzer`)
- **PDF Text Extraction:** Binary stream parsing using **Apache PDFBox 3.x**.
- **Objective ATS Scoring (0–100):** Transparent scoring algorithm evaluating section completeness, technical skill density, soft skills, action verbs, numerical metrics, and contact presence (Email, Phone, LinkedIn, GitHub).
- **Skill Gap Detection:** Identifies candidate's technical skills and flags high-priority missing skills for modern roles (Spring Boot, Microservices, Docker, Kafka, AWS, CI/CD).
- **Personalized Suggestions:** Concrete project improvement recommendations, certification guidance (Oracle Java SE, Spring Certified Professional), and formatting tips.

### 🎯 Module 5: Job Description Matcher (`/job-matcher`)
- Compare your uploaded PDF resume against any pasted job description.
- Computes **Job Match Percentage (%)** and match grade (*Strong Match*, *Good Match*, *Moderate Match*).
- Highlights **Matching Skills**, **Missing Skills**, **High-Frequency Keywords**, and targeted tailoring recommendations.

### 🤖 Module 6: AI Career Assistant (`/career-assistant`)
- Real-time interactive career coach for engineering and data candidates.
- **Dual Engine Architecture:**
  - *Gemini LLM API:* Active when an API key is provided in Settings.
  - *Java Knowledge Engine:* Built-in rule-based expert engine covering Java 21, Spring Boot, interview preparation, system design, and placement advice.
  - Full transparency: Responses clearly state whether they were generated by the Gemini LLM or the Java Knowledge Engine.
- Preset quick prompts for common interview and placement questions.

### ⏰ Module 7: Interview Reminders & Follow-Ups (`/reminders`)
- Schedule and manage technical interviews, HR follow-ups, and project deadlines.
- Categorized badges (`INTERVIEW`, `FOLLOW_UP`, `DEADLINE`, `OFFER_RESPONSE`, `GENERAL`).
- 1-click toggle for marking reminders completed.

### 🏢 Module 8: Companies Directory (`/companies`)
- Catalog prospective tech employers (Google, Infosys, Wipro, TCS, Accenture, PhonePe, Flipkart, Razorpay, Bosch).
- Automatic synchronization with application company entries.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Backend Language** | **Java 21 (LTS)** |
| **Framework** | **Spring Boot 3.3.4** |
| **Security** | **Spring Security 6**, BCrypt Password Encoder, CSRF Protection |
| **Persistence / ORM** | **Spring Data JPA**, **Hibernate 6** |
| **Template Engine** | **Thymeleaf**, Thymeleaf Extras Spring Security 6 |
| **PDF Processing** | **Apache PDFBox 3.0.3** |
| **Databases** | **H2 Database** (zero-config file persistence) & **MySQL 8.0+** |
| **Frontend UI** | HTML5, CSS3, **Bootstrap 5.3**, Bootstrap Icons, **Chart.js 4.4** |
| **Build & Tooling** | Apache Maven 3.9+ |

---

## 📁 Project Structure

```
Job Application Tracker/
├── src/
│   ├── main/
│   │   ├── java/com/tracker/
│   │   │   ├── JobTrackerApplication.java      # Application Main Entry Point
│   │   │   ├── config/                         # SecurityConfig, DataLoader, Seed Data
│   │   │   ├── controller/                     # MVC & REST Controllers (Auth, Dashboard, Apps, Resume, etc.)
│   │   │   ├── dto/                            # Data Transfer Objects & Validation Beans
│   │   │   ├── model/                          # JPA Entities (User, JobApplication, Resume, Reminder, etc.)
│   │   │   ├── repository/                     # Spring Data JPA Repositories (Multi-User Filtered)
│   │   │   ├── security/                       # CustomUserDetailsService
│   │   │   ├── service/                        # Business Logic & NLP Keyword Analyzers
│   │   │   └── util/                           # PdfTextExtractor, AtsKeywordDictionary
│   │   └── resources/
│   │       ├── static/                         # CSS (Purple/Lavender Theme), JS (Chart.js, Kanban, Chat)
│   │       ├── templates/                      # Thymeleaf Views (auth/, applications/, resume/, etc.)
│   │       └── application.properties          # Production Configuration
│   └── test/                                   # Full Test Suite (19/19 Unit & Integration Tests Passing)
├── build.bat                                   # One-click Windows Build Script
├── run.bat                                     # One-click Windows Start Script
├── pom.xml                                     # Maven Dependencies & Plugins
└── README.md                                   # Comprehensive Documentation
```

---

## ⚙️ How to Build and Run Locally

### Prerequisites
- Java 21 JDK (or use portable runtime inside `.tools/jdk-21.0.5+11`)
- Maven 3.9+ (or use portable executable inside `.tools/apache-maven-3.9.9`)

### Quick Start (Windows)
Double-click `run.bat` or run in terminal:
```cmd
run.bat
```

### Manual Maven Commands
```bash
# 1. Run unit and integration tests (19 tests)
mvn test

# 2. Package executable JAR
mvn clean package -DskipTests=true

# 3. Run application
java -jar target/job-tracker-1.0.0.jar
```

### Accessing the Web Application
Open your browser to:
👉 **[http://localhost:8080](http://localhost:8080)**

---

## 🧪 Automated Test Suite

The project includes an exhaustive automated test suite with 19 tests verifying:
- Executive Dashboard statistics calculations
- Full Job Application CRUD operations
- Kanban board status transitions
- Apache PDFBox text extraction from real PDF files
- Transparent ATS Scoring and skill gap detection
- Job description match percentages
- AI Career Assistant rule engine fallbacks
- Interview reminder scheduling
- User registration and validation
- Duplicate email rejection
- One-time password reset token validation
- **Strict multi-user data isolation between independent accounts**

To run tests:
```bash
mvn test
```

---

## 👩‍💻 Author & Repository

- **Author:** Sahana
- **GitHub Profile:** [https://github.com/sahanamadivalar99-hub](https://github.com/sahanamadivalar99-hub)
- **Repository:** `career-pulse-job-application-tracker`
- **Target Git Remote:** `https://github.com/sahanamadivalar99-hub/career-pulse-job-application-tracker.git`
