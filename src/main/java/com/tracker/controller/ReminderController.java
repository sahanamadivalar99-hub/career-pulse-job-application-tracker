package com.tracker.controller;

import com.tracker.model.Reminder;
import com.tracker.model.ReminderType;
import com.tracker.service.JobApplicationService;
import com.tracker.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reminders")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private JobApplicationService applicationService;

    @GetMapping
    public String viewReminders(Model model) {
        model.addAttribute("reminders", reminderService.getAllReminders());
        model.addAttribute("newReminder", new Reminder());
        model.addAttribute("reminderTypes", ReminderType.values());
        model.addAttribute("applications", applicationService.getAllApplications());
        model.addAttribute("activePage", "reminders");
        return "reminders/list";
    }

    @PostMapping("/save")
    public String saveReminder(@Valid @ModelAttribute("newReminder") Reminder reminder,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide all required fields for the reminder.");
            return "redirect:/reminders";
        }
        reminderService.saveReminder(reminder);
        redirectAttributes.addFlashAttribute("successMessage", "Reminder scheduled successfully!");
        return "redirect:/reminders";
    }

    @GetMapping("/toggle/{id}")
    public String toggleReminder(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        reminderService.toggleCompletion(id);
        redirectAttributes.addFlashAttribute("successMessage", "Reminder status updated.");
        return "redirect:/reminders";
    }

    @GetMapping("/delete/{id}")
    public String deleteReminder(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        reminderService.deleteReminder(id);
        redirectAttributes.addFlashAttribute("successMessage", "Reminder deleted.");
        return "redirect:/reminders";
    }
}
