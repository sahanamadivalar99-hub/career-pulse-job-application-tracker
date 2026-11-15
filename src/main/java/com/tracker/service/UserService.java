package com.tracker.service;

import com.tracker.dto.UserRegistrationDto;
import com.tracker.model.User;
import com.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    @Lazy
    private PasswordEncoder passwordEncoder;

    /**
     * Resolves the current authenticated user from Spring Security context.
     * Falls back to the primary demo user if unauthenticated or in tests.
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            // Check for OAuth2 authentication principal
            if (auth.getPrincipal() instanceof OAuth2User oauth2User) {
                String oauthEmail = oauth2User.getAttribute("email");
                if (oauthEmail != null && !oauthEmail.isBlank()) {
                    Optional<User> userOpt = userRepository.findByEmailIgnoreCase(oauthEmail.trim().toLowerCase());
                    if (userOpt.isPresent()) {
                        return userOpt.get();
                    }
                }
            }

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
                .orElse(null);
    }

    /**
     * Saves or loads a user authenticated via Google OAuth2.
     * If the user doesn't exist, automatically registers a new account.
     */
    @Transactional
    public User processOAuthPostLogin(String email, String name) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        String cleanEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(cleanEmail);
        if (userOpt.isPresent()) {
            User existingUser = userOpt.get();
            if ((existingUser.getFullName() == null || existingUser.getFullName().isBlank()) && name != null && !name.isBlank()) {
                existingUser.setFullName(name.trim());
                return userRepository.save(existingUser);
            }
            return existingUser;
        }

        User newUser = new User();
        newUser.setEmail(cleanEmail);
        newUser.setUsername(cleanEmail);
        newUser.setFullName(name != null && !name.isBlank() ? name.trim() : cleanEmail.split("@")[0]);
        newUser.setRole("ROLE_USER");
        newUser.setEnabled(true);
        newUser.setLocation("Bengaluru, Karnataka, India");
        newUser.setTargetRole("Java Full Stack Developer");
        newUser.setEducation("B.E. Engineering Student");
        newUser.setExperience("Fresher");
        newUser.setPreferredLocation("Bengaluru");
        newUser.setPreferredEmployment("Internship and Full-Time");
        newUser.setCreatedAt(LocalDateTime.now());
        return userRepository.save(newUser);
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
