package com.tracker.controller;

import com.tracker.dto.DashboardStatsDto;
import com.tracker.model.JobApplication;
import com.tracker.model.Reminder;
import com.tracker.model.Resume;
import com.tracker.service.JobApplicationService;
import com.tracker.service.ReminderService;
import com.tracker.service.ResumeParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private JobApplicationService applicationService;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private ResumeParserService resumeParserService;

    @GetMapping({"/", "/dashboard"})
    public String viewDashboard(Model model) {
        DashboardStatsDto stats = applicationService.getDashboardStatistics();
        List<JobApplication> recentApplications = applicationService.getRecentApplications();
        List<JobApplication> upcomingInterviews = applicationService.getUpcomingInterviews();
        List<Reminder> upcomingReminders = reminderService.getUpcomingReminders();
        Optional<Resume> latestResume = resumeParserService.getLatestResume();

        model.addAttribute("stats", stats);
        model.addAttribute("recentApplications", recentApplications);
        model.addAttribute("upcomingInterviews", upcomingInterviews);
        model.addAttribute("upcomingReminders", upcomingReminders);
        model.addAttribute("latestResume", latestResume.orElse(null));
        model.addAttribute("activePage", "dashboard");

        return "dashboard";
    }
}
