package com.tracker.controller;

import com.tracker.dto.UserRegistrationDto;
import com.tracker.model.User;
import com.tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String viewLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email address or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String viewRegisterPage(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@Valid @ModelAttribute("user") UserRegistrationDto dto,
                                      BindingResult bindingResult,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.registerUser(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully! Please sign in with your credentials.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/forgot-password")
    public String viewForgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        Model model) {
        if (email == null || email.isBlank()) {
            model.addAttribute("errorMessage", "Please provide a registered email address.");
            return "auth/forgot-password";
        }
        String token = userService.createPasswordResetToken(email.trim());
        if (token != null) {
            // Documented flow: If external SMTP is not configured in local environment, provide direct secure reset link
            model.addAttribute("resetToken", token);
            model.addAttribute("resetLink", "/reset-password?token=" + token);
            model.addAttribute("successMessage", "Password reset token generated. Since external SMTP mail services are not configured, you can proceed directly to reset your password below.");
        } else {
            model.addAttribute("errorMessage", "No registered account found with that email address.");
        }
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String viewResetPasswordPage(@RequestParam("token") String token, Model model) {
        if (userService.findByResetToken(token).isEmpty()) {
            model.addAttribute("errorMessage", "Invalid or expired password reset link.");
            return "auth/forgot-password";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (password == null || password.length() < 6) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "Password must be at least 6 characters long.");
            return "auth/reset-password";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "Passwords do not match.");
            return "auth/reset-password";
        }

        boolean success = userService.resetPasswordWithToken(token, password);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Your password has been reset successfully! Please sign in.");
            return "redirect:/login";
        } else {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "Password reset failed. The token may have expired.");
            return "auth/reset-password";
        }
    }

    @GetMapping("/profile")
    public String viewUserProfile(Model model) {
        User user = userService.getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("activePage", "profile");
        return "profile/index";
    }
}
