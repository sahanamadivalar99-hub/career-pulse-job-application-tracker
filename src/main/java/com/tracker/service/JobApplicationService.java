package com.tracker.service;

import com.tracker.dto.DashboardStatsDto;
import com.tracker.model.ApplicationStatus;
import com.tracker.model.JobApplication;
import com.tracker.model.ResumeAnalysis;
import com.tracker.model.User;
import com.tracker.repository.JobApplicationRepository;
import com.tracker.repository.ReminderRepository;
import com.tracker.repository.ResumeAnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class JobApplicationService {

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private ResumeAnalysisRepository resumeAnalysisRepository;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserService userService;

    public List<JobApplication> getAllApplications() {
        return getAllApplications(userService.getCurrentUser());
    }

    public List<JobApplication> getAllApplications(User user) {
        return applicationRepository.findAllByUserOrderByApplicationDateDesc(user);
    }

    public List<JobApplication> searchApplications(String keyword, ApplicationStatus status) {
        return searchApplications(keyword, status, userService.getCurrentUser());
    }

    public List<JobApplication> searchApplications(String keyword, ApplicationStatus status, User user) {
        return applicationRepository.searchApplicationsByUser(user, keyword, status);
    }

    public Optional<JobApplication> getApplicationById(Long id) {
        return getApplicationById(id, userService.getCurrentUser());
    }

    public Optional<JobApplication> getApplicationById(Long id, User user) {
        return applicationRepository.findByIdAndUser(id, user);
    }

    @Transactional
    public JobApplication saveApplication(JobApplication application) {
        return saveApplication(application, userService.getCurrentUser());
    }

    @Transactional
    public JobApplication saveApplication(JobApplication application, User user) {
        if (application.getUser() == null) {
            application.setUser(user);
        }
        if (application.getApplicationDate() == null) {
            application.setApplicationDate(LocalDate.now());
        }
        JobApplication saved = applicationRepository.save(application);

        // Auto-register company if it doesn't already exist
        if (saved.getCompanyName() != null && !saved.getCompanyName().isBlank()) {
            companyService.getOrCreateCompany(saved.getCompanyName(), saved.getLocation());
        }

        return saved;
    }

    @Transactional
    public JobApplication updateStatus(Long id, ApplicationStatus newStatus) {
        return updateStatus(id, newStatus, userService.getCurrentUser());
    }

    @Transactional
    public JobApplication updateStatus(Long id, ApplicationStatus newStatus, User user) {
        JobApplication application = applicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Application not found or unauthorized for id: " + id));
        application.setStatus(newStatus);
        return applicationRepository.save(application);
    }

    @Transactional
    public void deleteApplication(Long id) {
        deleteApplication(id, userService.getCurrentUser());
    }

    @Transactional
    public void deleteApplication(Long id, User user) {
        JobApplication app = applicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Application not found or unauthorized for id: " + id));
        applicationRepository.delete(app);
    }

    public Map<ApplicationStatus, List<JobApplication>> getApplicationsGroupedByStatus() {
        return getApplicationsGroupedByStatus(userService.getCurrentUser());
    }

    public Map<ApplicationStatus, List<JobApplication>> getApplicationsGroupedByStatus(User user) {
        Map<ApplicationStatus, List<JobApplication>> grouped = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            grouped.put(status, new ArrayList<>());
        }

        List<JobApplication> all = applicationRepository.findAllByUserOrderByApplicationDateDesc(user);
        for (JobApplication app : all) {
            grouped.computeIfAbsent(app.getStatus(), k -> new ArrayList<>()).add(app);
        }
        return grouped;
    }

    public List<JobApplication> getRecentApplications() {
        return getRecentApplications(userService.getCurrentUser());
    }

    public List<JobApplication> getRecentApplications(User user) {
        return applicationRepository.findTop5ByUserOrderByCreatedAtDesc(user);
    }

    public List<JobApplication> getUpcomingInterviews() {
        return getUpcomingInterviews(userService.getCurrentUser());
    }

    public List<JobApplication> getUpcomingInterviews(User user) {
        return applicationRepository.findUpcomingInterviewsByUser(user);
    }

    public DashboardStatsDto getDashboardStatistics() {
        return getDashboardStatistics(userService.getCurrentUser());
    }

    public DashboardStatsDto getDashboardStatistics(User user) {
        DashboardStatsDto stats = new DashboardStatsDto();

        long total = applicationRepository.countByUser(user);
        long applied = applicationRepository.countByUserAndStatus(user, ApplicationStatus.APPLIED);
        long inReview = applicationRepository.countByUserAndStatus(user, ApplicationStatus.IN_REVIEW);
        long interview = applicationRepository.countByUserAndStatus(user, ApplicationStatus.INTERVIEW);
        long offer = applicationRepository.countByUserAndStatus(user, ApplicationStatus.OFFER);
        long rejected = applicationRepository.countByUserAndStatus(user, ApplicationStatus.REJECTED);
        long reminders = reminderRepository.countByUserAndCompletedFalse(user);

        stats.setTotalApplications(total);
        stats.setAppliedCount(applied);
        stats.setInReviewCount(inReview);
        stats.setInterviewCount(interview);
        stats.setOfferCount(offer);
        stats.setRejectedCount(rejected);
        stats.setPendingRemindersCount(reminders);

        // Latest ATS score for this user
        Optional<ResumeAnalysis> latestAnalysis = resumeAnalysisRepository.findTopByResumeUserOrderByAnalyzedAtDesc(user);
        stats.setLatestAtsScore(latestAnalysis.map(ResumeAnalysis::getAtsScore).orElse(0));

        // Status breakdown for Doughnut Chart
        Map<String, Long> breakdown = new LinkedHashMap<>();
        breakdown.put("Applied", applied);
        breakdown.put("In Progress", inReview);
        breakdown.put("Interviews", interview);
        breakdown.put("Offers", offer);
        breakdown.put("Rejected", rejected);
        stats.setStatusBreakdown(breakdown);

        // Monthly trends (Last 6 months) for this user
        Map<String, Long> monthly = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter monthKeyFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter monthDisplayFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

        Map<String, String> keyToDisplay = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate m = now.minusMonths(i);
            String key = m.format(monthKeyFormatter);
            String label = m.format(monthDisplayFormatter);
            keyToDisplay.put(key, label);
            monthly.put(label, 0L);
        }

        List<JobApplication> all = applicationRepository.findAllByUser(user);
        for (JobApplication a : all) {
            if (a.getApplicationDate() != null) {
                String appKey = a.getApplicationDate().format(monthKeyFormatter);
                String label = keyToDisplay.get(appKey);
                if (label != null && monthly.containsKey(label)) {
                    monthly.put(label, monthly.get(label) + 1);
                }
            }
        }
        stats.setMonthlyTrend(monthly);

        return stats;
    }
}
