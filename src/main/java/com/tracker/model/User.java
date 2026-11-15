package com.tracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "role")
    private String role = "ROLE_USER";

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "reset_token", length = 1000)
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @Column(name = "target_role", length = 1000)
    private String targetRole;

    @Column(name = "location", length = 1000)
    private String location = "Bengaluru, Karnataka, India";

    @Column(name = "education", length = 1000)
    private String education = "B.E. Engineering Student";

    @Column(name = "experience", length = 1000)
    private String experience = "Fresher";

    @Column(name = "preferred_location", length = 1000)
    private String preferredLocation = "Bengaluru";

    @Column(name = "preferred_employment", length = 1000)
    private String preferredEmployment = "Internship and Full-Time";

    @Column(name = "gemini_api_key", length = 1000)
    private String geminiApiKey;

    @Column(name = "profile_picture", length = 1000)
    private String profilePicture;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public User(String username, String fullName, String email, String targetRole) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.targetRole = targetRole;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public String getPreferredEmployment() {
        return preferredEmployment;
    }

    public void setPreferredEmployment(String preferredEmployment) {
        this.preferredEmployment = preferredEmployment;
    }

    public String getGeminiApiKey() {
        return geminiApiKey;
    }

    public void setGeminiApiKey(String geminiApiKey) {
        this.geminiApiKey = geminiApiKey;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getPicture() {
        return profilePicture;
    }

    public void setPicture(String picture) {
        this.profilePicture = picture;
    }
}
