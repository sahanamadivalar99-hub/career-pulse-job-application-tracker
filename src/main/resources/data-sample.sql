-- ==============================================================
-- Smart Job Application Tracker with AI Resume Analyzer
-- Sample Demonstration Data for MySQL
-- ==============================================================

USE job_tracker_db;

-- 1. Sample User
INSERT INTO users (username, password, full_name, email, target_role) 
VALUES ('developer', 'password123', 'Sahana', 'sahana@example.com', 'Java Full-Stack Developer')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 2. Sample Companies
INSERT INTO companies (name, industry, location, website, rating) VALUES
('Google', 'Technology / Cloud', 'Mountain View, CA / Remote', 'https://careers.google.com', 4.8),
('Amazon', 'Cloud & E-Commerce', 'Seattle, WA / Remote', 'https://amazon.jobs', 4.5),
('Microsoft', 'Enterprise Software', 'Redmond, WA / Hybrid', 'https://careers.microsoft.com', 4.7),
('Netflix', 'Streaming & Entertainment', 'Los Gatos, CA / Remote', 'https://jobs.netflix.com', 4.6),
('Spotify', 'Audio & Streaming', 'New York, NY / Remote', 'https://lifeatspotify.com', 4.7),
('Uber', 'Transportation & Mobility', 'San Francisco, CA / Hybrid', 'https://uber.com/careers', 4.2)
ON DUPLICATE KEY UPDATE industry=VALUES(industry);

-- 3. Sample Job Applications
INSERT INTO applications (company_name, job_role, location, application_date, status, salary_range, job_description, notes) VALUES
('Google', 'Java Backend Engineer', 'Mountain View, CA (Hybrid)', CURDATE() - INTERVAL 12 DAY, 'INTERVIEW', '$145,000 - $175,000', 'Java 21, Spring Boot, distributed microservices, gRPC, and high-throughput databases.', 'Round 2 Technical Interview scheduled.'),
('Microsoft', 'Full Stack Developer', 'Redmond, WA (Remote)', CURDATE() - INTERVAL 25 DAY, 'OFFER', '$140,000 - $165,000', 'Full-stack role developing enterprise SaaS applications using Java, Spring Cloud, Azure, and RESTful APIs.', 'Received written offer letter!'),
('Amazon AWS', 'Software Development Engineer II', 'Seattle, WA', CURDATE() - INTERVAL 8 DAY, 'IN_REVIEW', '$150,000 - $180,000', 'AWS SDE II position focusing on distributed cloud services, high-availability architecture, DynamoDB, Java.', 'Completed Online Assessment with 100% test cases passed.'),
('Spotify', 'Backend Java Developer', 'New York, NY (Remote)', CURDATE() - INTERVAL 3 DAY, 'APPLIED', '$130,000 - $155,000', 'Building scalable event-driven audio streaming microservices with Java, Apache Kafka, Spring Boot, and PostgreSQL.', 'Applied via company portal.'),
('Netflix', 'Cloud Platform Engineer', 'Los Gatos, CA', CURDATE() - INTERVAL 5 DAY, 'APPLIED', '$160,000 - $190,000', 'Developing resilient fault-tolerant distributed infrastructure using Java, Spring, Docker, and Kubernetes.', 'Submitted application through employee referral.'),
('Uber', 'Infrastructure Engineer', 'San Francisco, CA', CURDATE() - INTERVAL 20 DAY, 'REJECTED', '$140,000 - $170,000', 'Role required 5+ years of C++ kernel development. Received polite automated rejection letter.', 'Focus on Java/Cloud roles going forward.');

-- 4. Sample Reminders
INSERT INTO reminders (title, type, reminder_date, reminder_time, is_completed, notes) VALUES
('Google Technical Screen: System Design & Concurrency', 'INTERVIEW', CURDATE() + INTERVAL 3 DAY, '14:00:00', FALSE, 'Review Java 21 Virtual Threads and high-concurrency rate limiting.'),
('Follow up with Amazon AWS Recruiter', 'FOLLOW_UP', CURDATE() + INTERVAL 2 DAY, '10:30:00', FALSE, 'Inquire about next steps after completing the online assessment.'),
('Respond to Microsoft Offer Letter', 'DEADLINE', CURDATE() + INTERVAL 5 DAY, '17:00:00', FALSE, 'Review benefits package and finalize decision.');
