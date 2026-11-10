package com.tracker.service;

import com.tracker.dto.UserRegistrationDto;
import com.tracker.model.User;
import com.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Resolves the current authenticated user from Spring Security context.
     * Falls back to the primary demo user if unauthenticated or in tests.
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String identifier = auth.getName();
            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(identifier)
                    .or(() -> userRepository.findByUsernameIgnoreCase(identifier));
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        }

        // Fallback for demo or test environments
        return userRepository.findByEmailIgnoreCase("sahana.bengaluru@example.com")
                .or(() -> userRepository.findByUsername("developer"))
                .or(() -> userRepository.findFirstByOrderByIdAsc())
                .orElseGet(() -> {
                    User defaultUser = new User("developer", "Sahana", "sahana.bengaluru@example.com", "Java Full Stack Developer / AI & Data Engineer");
                    defaultUser.setPassword(passwordEncoder.encode("Sahana@123"));
                    defaultUser.setRole("ROLE_USER");
                    defaultUser.setEnabled(true);
                    return userRepository.save(defaultUser);
                });
    }

    /**
     * Registers a new user with BCrypt hashed password and validation checks.
     */
    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match.");
        }

        User user = new User();
        user.setUsername(dto.getEmail().trim().toLowerCase());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setFullName(dto.getFullName().trim());
        user.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setLocation(dto.getLocation() != null ? dto.getLocation().trim() : "Bengaluru, Karnataka, India");
        user.setTargetRole(dto.getTargetRole() != null ? dto.getTargetRole().trim() : "Java Full Stack Developer");
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Creates a secure password reset token valid for 1 hour.
     */
    @Transactional
    public String createPasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            return null;
        }
        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        return token;
    }

    /**
     * Resets password using a validated token.
     */
    @Transactional
    public boolean resetPasswordWithToken(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        return true;
    }

    public Optional<User> findByResetToken(String token) {
        return userRepository.findByResetToken(token);
    }

    @Transactional
    public User updateSettings(String fullName, String email, String targetRole, String geminiApiKey) {
        return updateSettings(fullName, email, targetRole, null, null, null, null, null, geminiApiKey);
    }

    @Transactional
    public User updateSettings(String fullName, String email, String targetRole, String location,
                               String education, String experience, String preferredLocation,
                               String preferredEmployment, String geminiApiKey) {
        User user = getCurrentUser();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setTargetRole(targetRole);
        if (location != null && !location.isBlank()) user.setLocation(location);
        if (education != null && !education.isBlank()) user.setEducation(education);
        if (experience != null && !experience.isBlank()) user.setExperience(experience);
        if (preferredLocation != null && !preferredLocation.isBlank()) user.setPreferredLocation(preferredLocation);
        if (preferredEmployment != null && !preferredEmployment.isBlank()) user.setPreferredEmployment(preferredEmployment);
        user.setGeminiApiKey(geminiApiKey != null && !geminiApiKey.isBlank() ? geminiApiKey.trim() : null);
        return userRepository.save(user);
    }
}
