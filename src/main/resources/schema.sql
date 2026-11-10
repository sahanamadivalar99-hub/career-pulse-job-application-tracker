-- ==============================================================
-- Smart Job Application Tracker with AI Resume Analyzer
-- MySQL Database Schema
-- ==============================================================

CREATE DATABASE IF NOT EXISTS job_tracker_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE job_tracker_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255),
    full_name VARCHAR(150),
    email VARCHAR(150),
    target_role VARCHAR(150),
    gemini_api_key VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Companies Table
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    industry VARCHAR(150),
    website VARCHAR(255),
    location VARCHAR(150),
    contact_email VARCHAR(150),
    rating DOUBLE DEFAULT 4.0,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Job Applications Table
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    job_role VARCHAR(150) NOT NULL,
    job_description TEXT,
    location VARCHAR(150),
    application_date DATE NOT NULL,
    job_link VARCHAR(500),
    salary_range VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'APPLIED',
    notes TEXT,
    interview_date TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_company_name (company_name),
    INDEX idx_status (status),
    INDEX idx_application_date (application_date)
);

-- 4. Resumes Table
CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    file_path VARCHAR(500),
    extracted_text LONGTEXT,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Resume Analysis Table
CREATE TABLE IF NOT EXISTS resume_analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL UNIQUE,
    ats_score INT NOT NULL,
    technical_skills TEXT,
    missing_skills TEXT,
    soft_skills TEXT,
    missing_sections TEXT,
    project_suggestions TEXT,
    certification_recommendations TEXT,
    summary_suggestions TEXT,
    formatting_suggestions TEXT,
    word_count INT,
    contact_info_present BOOLEAN,
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_analysis_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
);

-- 6. Reminders Table
CREATE TABLE IF NOT EXISTS reminders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'INTERVIEW',
    reminder_date DATE NOT NULL,
    reminder_time TIME,
    is_completed BOOLEAN DEFAULT FALSE,
    notes TEXT,
    application_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminder_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE SET NULL,
    INDEX idx_reminder_date (reminder_date)
);

-- 7. Job Matches Table
CREATE TABLE IF NOT EXISTS job_matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_title VARCHAR(150) NOT NULL,
    company_name VARCHAR(150),
    job_description LONGTEXT NOT NULL,
    resume_id BIGINT,
    match_percentage INT NOT NULL,
    matching_skills TEXT,
    missing_skills TEXT,
    important_keywords TEXT,
    recommendations TEXT,
    matched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_match_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE SET NULL
);

-- 8. AI Chat Messages Table
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(50) NOT NULL,
    content LONGTEXT NOT NULL,
    provider VARCHAR(50) DEFAULT 'RULE_BASED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
