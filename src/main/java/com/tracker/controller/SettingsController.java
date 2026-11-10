package com.tracker.controller;

import com.tracker.model.User;
import com.tracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewSettings(Model model) {
        User user = userService.getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("activePage", "settings");
        return "settings/index";
    }

    @PostMapping("/save")
    public String saveSettings(@RequestParam("fullName") String fullName,
                               @RequestParam("email") String email,
                               @RequestParam("targetRole") String targetRole,
                               @RequestParam(value = "location", required = false) String location,
                               @RequestParam(value = "education", required = false) String education,
                               @RequestParam(value = "experience", required = false) String experience,
                               @RequestParam(value = "preferredLocation", required = false) String preferredLocation,
                               @RequestParam(value = "preferredEmployment", required = false) String preferredEmployment,
                               @RequestParam(value = "geminiApiKey", required = false) String geminiApiKey,
                               RedirectAttributes redirectAttributes) {
        userService.updateSettings(fullName, email, targetRole, location, education, experience, preferredLocation, preferredEmployment, geminiApiKey);
        redirectAttributes.addFlashAttribute("successMessage", "Settings and profile updated successfully!");
        return "redirect:/settings";
    }
}
