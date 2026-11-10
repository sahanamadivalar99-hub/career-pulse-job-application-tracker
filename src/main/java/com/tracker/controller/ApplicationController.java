package com.tracker.controller;

import com.tracker.model.ApplicationStatus;
import com.tracker.model.JobApplication;
import com.tracker.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    private JobApplicationService applicationService;

    @GetMapping
    public String listApplications(@RequestParam(value = "keyword", required = false) String keyword,
                                   @RequestParam(value = "status", required = false) ApplicationStatus status,
                                   Model model) {
        List<JobApplication> applications;
        if ((keyword != null && !keyword.isBlank()) || status != null) {
            applications = applicationService.searchApplications(keyword, status);
        } else {
            applications = applicationService.getAllApplications();
        }

        model.addAttribute("applications", applications);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", ApplicationStatus.values());
        model.addAttribute("activePage", "applications");
        model.addAttribute("viewMode", "list");

        return "applications/list";
    }

    @GetMapping("/kanban")
    public String kanbanView(Model model) {
        Map<ApplicationStatus, List<JobApplication>> grouped = applicationService.getApplicationsGroupedByStatus();
        model.addAttribute("groupedApplications", grouped);
        model.addAttribute("statuses", ApplicationStatus.values());
        model.addAttribute("activePage", "applications");
        model.addAttribute("viewMode", "kanban");

        return "applications/kanban";
    }

    @GetMapping("/new")
    public String newApplicationForm(Model model) {
        model.addAttribute("application", new JobApplication());
        model.addAttribute("statuses", ApplicationStatus.values());
        model.addAttribute("activePage", "applications");
        return "applications/form";
    }

    @PostMapping("/save")
    public String saveApplication(@Valid @ModelAttribute("application") JobApplication application,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", ApplicationStatus.values());
            model.addAttribute("activePage", "applications");
            return "applications/form";
        }

        applicationService.saveApplication(application);
        redirectAttributes.addFlashAttribute("successMessage", "Job application saved successfully!");
        return "redirect:/applications";
    }

    @GetMapping("/{id}")
    public String viewApplicationDetail(@PathVariable("id") Long id, Model model) {
        JobApplication application = applicationService.getApplicationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid application Id:" + id));
        model.addAttribute("app", application);
        model.addAttribute("activePage", "applications");
        return "applications/detail";
    }

    @GetMapping("/edit/{id}")
    public String editApplicationForm(@PathVariable("id") Long id, Model model) {
        JobApplication application = applicationService.getApplicationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid application Id:" + id));
        model.addAttribute("application", application);
        model.addAttribute("statuses", ApplicationStatus.values());
        model.addAttribute("activePage", "applications");
        return "applications/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteApplication(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        applicationService.deleteApplication(id);
        redirectAttributes.addFlashAttribute("successMessage", "Job application deleted successfully.");
        return "redirect:/applications";
    }

    @PostMapping("/api/status")
    @ResponseBody
    public ResponseEntity<?> updateStatusAjax(@RequestParam("id") Long id,
                                              @RequestParam("status") ApplicationStatus status) {
        try {
            JobApplication updated = applicationService.updateStatus(id, status);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "id", updated.getId(),
                "newStatus", updated.getStatus().name()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
